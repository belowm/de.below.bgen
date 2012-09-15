package de.below.bgen.builder.generator;

import static de.below.bgen.builder.generator.Expressions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import de.below.bgen.builder.generator.Expressions.ObjectVar;
import de.below.bgen.builder.generator.Property.SetterProperty;
import de.below.bgen.builder.generator.components.InstantiationStrategy;
import de.below.bgen.builder.generator.components.SetterNamingStrategy;
import de.below.bgen.builder.generator.components.TargetTypeCreationStrategy;
import de.below.bgen.codegen.Expression;
import de.below.bgen.codegen.Visibility;
import de.below.bgen.util.JdtUtils;
import de.below.codegen.ClassBuilder;
import de.below.codegen.ClassBuilder.InClassStep;
import de.below.codegen.JavaCodeWriter;
import de.below.codegen.MethodBuilder.InMethodStep;
import de.below.rcp.widgets.Pair;

public class BuilderGenerator {

	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private final SetterNamingStrategy setterNaming;
	private final TargetTypeCreationStrategy target;

	private final InstantiationStrategy instantiationStrategy;

	private final IProgressMonitor progressMonitor;

	private final PropertyCollector propertyCollector = new PropertyCollector();

	private final boolean fluentSetters;

	public BuilderGenerator(IProgressMonitor pm,
			SetterNamingStrategy setterNaming,
			TargetTypeCreationStrategy target,
			InstantiationStrategy instantiationStrategy, boolean fluentSetters) {

		this.progressMonitor = pm;
		this.setterNaming = setterNaming;
		this.target = target;
		this.instantiationStrategy = instantiationStrategy;
		this.fluentSetters = fluentSetters;
	}

	/**
	 * Creates the builder.
	 */
	public void generate(IType sourceType, List<IMethod> setterMethods,
			List<String> mandatoryProperties, String builderName)
			throws JavaModelException {

		if (sourceType == null) {
			throw new IllegalArgumentException("no source type given");
		}

		IMethod instantiationMethod = instantiationStrategy.getMethod();

		PropertyList properties = propertyCollector.collectProperties(
				sourceType, instantiationMethod, setterMethods);

		InClassStep<JavaCodeWriter> builder = ClassBuilder.newClass()
				.visibility(Visibility.PUBLIC)
				.staticClass(!target.isMainType())
				.name(builderName);

		createFactoryMethod(builderName, builder);

		createBuildUponMethod(sourceType, properties, builder, builderName);

		createFields(properties.getConstructorArgs(), builder);
		createFields(properties.getSetterProperties(), builder);

		createSetters(properties.getConstructorArgs(), builder, builderName);
		createSetters(properties.getSetterProperties(), builder, builderName);

		createBuildMethod(sourceType, properties, instantiationMethod, builder);


		String builderSourceCode = builder.endClass().render();

		IType builderType = target.createTargetType(progressMonitor, sourceType, builderName,
				builderSourceCode);
		
		format(builderType);
	}

	private void createFields(List<? extends Property> properties,
			InClassStep<JavaCodeWriter> builder) {

		for (Property property : properties) {

			builder.beginField().type(property.getType())
					.name(property.getName()).visibility(Visibility.PRIVATE)
					.buildField();
		}

	}

	/**
	 * Creates the static "buildUpon(originalObject)" - method, which can be
	 * used to create a builder that is populated with the values from a given
	 * object.
	 * 
	 * @param sourceType
	 *            The class for which the builder is created.
	 * @param properties
	 * @param builder
	 * @param builderName
	 * @throws JavaModelException
	 */
	private void createBuildUponMethod(IType sourceType,
			PropertyList properties, InClassStep<?> builder,
			String builderName) throws JavaModelException {

		List<Pair<Property, IMethod>> getterMethod = findGetterMethodsForBuilderPropertiesOnOriginalType(
				sourceType, properties);

		Set<String> exceptions = new HashSet<String>();
		
		// add checked exceptions that can encounter when calling the
		// getter-methods on the original builder
		for (Pair<Property, IMethod> getterAndProperty : getterMethod) {
			
			for (String exc : getterAndProperty.getRight().getExceptionTypes()) {
				exceptions.add(Signature.toString(exc));
			}
			
		}
		
		InMethodStep<?> method = builder
				.beginMethod().visibility(Visibility.PUBLIC)
				.staticMethod()
				.returnType(builderName)
				.exceptions(exceptions)
				.argument(sourceType.getElementName(), "original")
				.name("buildUpon");


		method.addStatement(Expressions.declarationWithAssignment(
				type(builderName), variable("builder"),
				methodCall(type(builderName), "newBuilder")));
		
		
		for (Pair<Property, IMethod> getterAndProp : getterMethod) {
			
			String setterName = setterNaming.renderSetterNameFor(getterAndProp.getLeft().getName());
			
			method.addStatement(methodCall(
					object("builder"), setterName, methodCall(object("original"), getterAndProp.getRight().getElementName())));
		}
		
		method.addStatement(Expressions.returnStatement(object("builder")));
		method.endMethod();

	}

	/**
	 * Gets all properties for which a getter-method exist on the target type (
	 * this is needed, because a class may have more setters then getters)
	 */
	private List<Pair<Property,IMethod>> findGetterMethodsForBuilderPropertiesOnOriginalType(
			IType sourceType, PropertyList properties)
			throws JavaModelException {

		List<Pair<Property, IMethod>> result = new ArrayList<Pair<Property,IMethod>>();

		for (Property property : properties.getAll()) {
			IMethod method = sourceType.getMethod(property.renderGetterName(),
					EMPTY_STRING_ARRAY);

			if (method.exists() && JdtUtils.isPublic(method)) {
				result.add(Pair.of(property, method));
			}

		}

		return result;
	}

	/**
	 * Creates a setter-method for each property of the intrinsic class.
	 */
	private void createSetters(List<? extends Property> properties,
			final InClassStep<JavaCodeWriter> builder, String builderName)
			throws JavaModelException {

		for (Property property : properties) {

			InMethodStep<InClassStep<JavaCodeWriter>> statements = builder.beginMethod()
					.visibility(Visibility.PUBLIC)
					.returnType(fluentSetters ? builderName : null)
					.argument(property.getType(), property.getName())
					.name(setterNaming.renderSetterNameFor(property.getName()));
			
			statements.addStatement(assignment(type("this"), variable(property.getName()), 
					variable(property.getName())));
			
			if (fluentSetters) {
				statements.addStatement(returnStatement(object("this")));
			}
							
			statements.endMethod();
		}

	}

	/**
	 * Creates the build()-method on the builder, that is responsible for
	 * instantiating the intrinsic type.
	 */
	private void createBuildMethod(IType sourceType, PropertyList properties,
			IMethod instantiationMethod, InClassStep<JavaCodeWriter> builder)
			throws JavaModelException {
		
		Set<String> exceptions = collectExceptions(properties,
				instantiationMethod);
		
		InMethodStep<InClassStep<JavaCodeWriter>> result = builder
				.beginMethod()
				.visibility(Visibility.PUBLIC)
				.returnType(sourceType.getElementName())
				.exceptions(exceptions)
				.name("build");

		ObjectVar builderInstanceVar = object("result");
		
		renderInstantiationMethodCall(sourceType, properties, instantiationMethod, result,
				builderInstanceVar);

		renderSetterCalls(properties, result, builderInstanceVar);

		result.addStatement(Expressions.returnStatement(builderInstanceVar));

		result.endMethod();

	}

	/**
	 * Renders the the code that calls the setter-methods on the target type.
	 */
	private void renderSetterCalls(PropertyList properties,
			InMethodStep<InClassStep<JavaCodeWriter>> builder,
			ObjectVar builderVar) {
		
		for (Property property : properties.getSetterProperties()) {
			builder.addStatement(Expressions.methodCall(builderVar, property.renderSetterName(),
					variable(property.getName())));
		}
	}

	/**
	 * Renders the code that instantiates the target type (via constructor or
	 * static factory method)
	 */
	private void renderInstantiationMethodCall(IType sourceType, PropertyList properties,
			IMethod instantiationMethod,
			InMethodStep<InClassStep<JavaCodeWriter>> result,
			ObjectVar builderVar) throws JavaModelException {
		if (instantiationMethod != null) {

			List<Expression> args = new ArrayList<Expression>();
			for (Property property : properties.getConstructorArgs()) {
				args.add(variable(property.getName()));
			}
			
			Expression instantiation = renderInstantiation(sourceType, instantiationMethod, args);
			
			result.addStatement(declarationWithAssignment(
					type(sourceType.getElementName()), builderVar,
					instantiation));

		}
	}

	/**
	 * Returns all checked exceptions that may be thrown when instantiating the
	 * target type or when calling one of its setter methods.
	 */
	private Set<String> collectExceptions(PropertyList properties,
			IMethod instantiationMethod) throws JavaModelException {
		
		Set<String> exceptions = new HashSet<String>();
		
		if (instantiationMethod != null) {
			for (String exc : instantiationMethod.getExceptionTypes()) {
				exceptions.add(Signature.toString(exc));
			}
		}
		
		for (SetterProperty property : properties.getSetterProperties()) {
			for (String exc : property.getSetter().getExceptionTypes()) {
				exceptions.add(Signature.toString(exc));
			}
		}
		return exceptions;
	}

	/**
	 * Renders the call to the instantiation-method (which may either be a
	 * constructor, or a static factory method.
	 */
	private Expression renderInstantiation(IType sourceType, IMethod instantiationMethod,
			List<Expression> args) throws JavaModelException {

		if (instantiationMethod.isConstructor()) {
			return instantiation(type(sourceType.getElementName()), args);
		}
		else if (JdtUtils.isStatic(instantiationMethod)){
			return methodCall(type(instantiationMethod.getDeclaringType().getElementName()), 
					instantiationMethod.getElementName(), args);
		}
		else {
			throw new IllegalArgumentException("invalid instantiation method: " + instantiationMethod);
		}
	}

	private void format(IType builderType) throws JavaModelException {
		target.format(builderType);
	}

	/**
	 * Creates the static factory-method "newBuilder" on the builder type.
	 * 
	 * @param builderName
	 *            Name of the builder.
	 */
	private void createFactoryMethod(String builderName,
			InClassStep<JavaCodeWriter> builder) throws JavaModelException {

		builder.beginMethod().visibility(Visibility.PUBLIC).staticMethod()
				.returnType(builderName)
				.name("newBuilder")
				.addStatement(returnStatement(instantiation(type(builderName))))
				.endMethod();

	}

}
