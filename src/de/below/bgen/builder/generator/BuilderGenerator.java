package de.below.bgen.builder.generator;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import de.below.bgen.builder.generator.components.FieldGenerationStrategy;
import de.below.bgen.builder.generator.components.InstantiationStrategy;
import de.below.bgen.builder.generator.components.SetterGenerationStrategy;
import de.below.bgen.builder.generator.components.SetterNamingStrategy;
import de.below.bgen.builder.generator.components.TargetTypeCreationStrategy;
import de.below.bgen.util.CodeGenUtils;
import de.below.bgen.util.Handler;

public class BuilderGenerator {

	private static final String BUILD_METHOD = "public %1$s build()%4$s { \n\t%1$s result = %2$s; \n\t%3$s \n\treturn result;\n}";

	private static final String FACTORY_METHOD = "public static %1$s newBuilder() { \n\treturn new %1$s(); \n}";

	private final SetterGenerationStrategy setterGenerator;
	private final FieldGenerationStrategy fieldGenerator;
	private final SetterNamingStrategy setterNaming;
	private final TargetTypeCreationStrategy target;

	private final InstantiationStrategy instantiationStrategy;

	private final IProgressMonitor progressMonitor;
	
	private final PropertyCollector propertyCollector = new PropertyCollector();

	public BuilderGenerator(IProgressMonitor pm,
			SetterGenerationStrategy setterGenerator,
			FieldGenerationStrategy fieldGenerator,
			SetterNamingStrategy setterNaming,
			TargetTypeCreationStrategy target,
			InstantiationStrategy instantiationStrategy) {

		this.progressMonitor = pm;
		this.setterGenerator = setterGenerator;
		this.fieldGenerator = fieldGenerator;
		this.setterNaming = setterNaming;
		this.target = target;
		this.instantiationStrategy = instantiationStrategy;
	}

	/**
	 * Creates a builder for the given source type in the given target type.
	 * 
	 * @param sourceType
	 * @param setterMethods
	 * @param builderName
	 * @param constructor
	 * @param targetCompilationUnit
	 * @throws JavaModelException
	 */
	public void generate(IType sourceType, List<IMethod> setterMethods,
			List<String> mandatoryProperties, String builderName)
			throws JavaModelException {

		if (sourceType == null) {
			throw new IllegalArgumentException("no source type given");
		}

		PropertyList properties = propertyCollector.collectProperties(
				sourceType, instantiationStrategy.getMethod(), setterMethods);

		IType builderType = target.createTargetType(progressMonitor,
				sourceType, builderName, null);

		createFactoryMethod(sourceType, builderType);

//		createStepInterfaces(properties, sourceType, builderType);

		createSettersAndFields(properties, builderType);
		createBuildMethod(sourceType, instantiationStrategy.getMethod(),
				builderType, setterMethods);
		createBuildUponMethod(sourceType, builderType);

		format(builderType);

	}

	private void createBuildUponMethod(IType sourceType, IType builderType)
			throws JavaModelException {

		StringBuilder copyStatements = new StringBuilder();

		for (IMethod method : sourceType.getMethods()) {

			String methodName = method.getElementName();
			if (methodName.startsWith("get") && methodName.length() > 3) {

				String setterMethodName = setterNaming
						.renderSetterNameFor(methodName.substring(3));

				IMethod setterMethod = builderType.getMethod(setterMethodName,
						new String[] { method.getReturnType() });

				if (setterMethod.exists()) {
					copyStatements.append("\tbuilder.").append(
							setterMethod.getElementName());
					copyStatements.append("(original.")
							.append(method.getElementName()).append("());\n");
				}
			}
		}

		if (copyStatements.length() > 0) {
			String contents = new StringBuilder().append("public static ")
					.append(builderType.getElementName()).append(" buildUpon(")
					.append(sourceType.getElementName())
					.append(" original) {\n").append("\t")
					.append(builderType.getElementName())
					.append(" builder = newBuilder();\n")
					.append(copyStatements).append("\treturn builder;")
					.append("\n}").toString();

			IType methodHome = getStaticMethodHome(sourceType, builderType);

			methodHome.createMethod(contents, null, true, progressMonitor);

		}

	}

	private IType getStaticMethodHome(IType sourceType, IType builderType) {

		if (sourceType.getCompilationUnit() != null) {
			if (sourceType.getCompilationUnit().equals(
					builderType.getCompilationUnit()))
				return sourceType;
		}

		return builderType;
	}

	private void createSettersAndFields(PropertyList properties,
			final IType builderType) throws JavaModelException {

		
		properties.visitAll(new Handler<Property>() {

			@Override
			public void handle(Property item) {
				try {
					createSetterAndField(builderType, item);
				} 
				catch (JavaModelException e) {
					throw new RuntimeException("error creating setter for " + item, e);
				}
			}
		});


	}
	private void createSetterAndField(IType builderType, Property property)
			throws JavaModelException {
		builderType.createField(
				fieldGenerator.renderFieldCode(property.getName(),
						property.getType()), null, true, progressMonitor);

		String setterCode = setterGenerator.renderSetterCode(setterNaming,
				builderType, property.getName(), property.getType());

		builderType.createMethod(setterCode, null, true, progressMonitor);

	}

	private IMethod createBuildMethod(IType sourceType, IMethod constructor,
			IType builderType, List<IMethod> setterMethods) throws JavaModelException {

		StringBuilder setterCalls = renderSetterCalls(setterMethods);

		String exceptions = renderExceptions(constructor);

		String instantiation = instantiationStrategy.renderInstantiation();

		String buildMethodSource = String.format(BUILD_METHOD,
				sourceType.getElementName(), instantiation, setterCalls,
				exceptions);

		IMethod buildMethod = builderType.createMethod(buildMethodSource, null,
				true, progressMonitor);

		return buildMethod;
	}

	private String renderExceptions(IMethod constructor)
			throws JavaModelException {
		StringBuilder exceptions = new StringBuilder();

		if (constructor != null) {
			String[] exceptionTypes = constructor.getExceptionTypes();

			for (String exception : exceptionTypes) {

				if (exceptions.length() == 0) {
					exceptions.append(" throws ");
				} else {
					exceptions.append(", ");
				}

				exceptions
						.append(exception.substring(1, exception.length() - 1));
			}
		}
		return exceptions.toString();
	}

	private StringBuilder renderSetterCalls(List<IMethod> setterMethods) {
		StringBuilder setterCalls = new StringBuilder();

		for (IMethod method : setterMethods) {

			setterCalls.append("\n\tresult.").append(method.getElementName())
					.append("(").append(CodeGenUtils.getPropertyNameFromAccessorMethod(method.getElementName()))
					.append(");");

		}

		return setterCalls;
	}

	private void format(IType builderType) throws JavaModelException {
		target.format(builderType);
	}

	private void createFactoryMethod(IType source, IType builder)
			throws JavaModelException {

		IType staticMethodHome = getStaticMethodHome(source, builder);

		staticMethodHome.createMethod(
				String.format(FACTORY_METHOD, builder.getElementName()), null,
				true, progressMonitor);

	}

}
