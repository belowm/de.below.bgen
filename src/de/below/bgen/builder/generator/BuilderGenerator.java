package de.below.bgen.builder.generator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import de.below.bgen.builder.generator.Property.ConstructorArgument;
import de.below.bgen.builder.generator.components.FieldGenerationStrategy;
import de.below.bgen.builder.generator.components.InstantiationStrategy;
import de.below.bgen.builder.generator.components.SetterGenerationStrategy;
import de.below.bgen.builder.generator.components.SetterNamingStrategy;
import de.below.bgen.builder.generator.components.TargetTypeCreationStrategy;
import de.below.bgen.util.Handler;
import de.below.bgen.util.StringUtil;

public class BuilderGenerator {

	private static final String BUILD_METHOD = "public %1$s build()%4$s { \n\t%1$s result = %2$s; \n\t%3$s \n\treturn result;\n}";

	private static final String FACTORY_METHOD = "public static %1$s newBuilder() { \n\treturn new %1$s(); \n}";

	private final SetterGenerationStrategy setterGenerator;
	private final FieldGenerationStrategy fieldGenerator;
	private final SetterNamingStrategy setterNaming;
	private List<IMethod> setterMethods;
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

		this.setterMethods = setterMethods;

		if (sourceType == null) {
			throw new IllegalArgumentException("no source type given");
		}

		PropertyList properties = propertyCollector.collectProperties(
				sourceType, instantiationStrategy.getMethod(), setterMethods);

		IType builderType = target.createBuilderType(progressMonitor,
				sourceType, builderName);

		createFactoryMethod(sourceType, builderType);

//		createStepInterfaces(properties, sourceType, builderType);

		createSettersAndFields(properties, builderType);
		createBuildMethod(sourceType, instantiationStrategy.getMethod(),
				builderType);
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

	private List<IType> createStepInterfaces(PropertyList properties, IType sourceType,
			IType builderType) throws JavaModelException {

		List<IType> result = new ArrayList<IType>();

		IType nextStep = createStepInterface(builderType, null, "Optional");
		createBuildMethod(sourceType, instantiationStrategy.getMethod(),
				nextStep);

		result.add(nextStep);

		List<ConstructorArgument> constructorArgs = properties.getConstructorArgs();
		
		for (int i = constructorArgs.size() - 1; i > -1; i--) {

			ConstructorArgument property = constructorArgs.get(i);
			IType stepInterface = createStepInterface(builderType, nextStep,
					property.getName());
			createStepInterfaceMethod(stepInterface, nextStep, property.getType(),
					property.getName());
			
			nextStep = stepInterface;
			result.add(stepInterface);
		}

		return result;

	}

	private void createStepInterfaceMethod(IType stepInterface, IType nextStep,
			String propertyType, String propertyName) throws JavaModelException {

		String setterCode = setterGenerator.renderSetterDeclaration(
				setterNaming, nextStep, propertyName, propertyType);
		stepInterface.createMethod(setterCode, null, true, progressMonitor);

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

	private IType createStepInterface(IType builderType, IType nextStep,
			String propertyName) throws JavaModelException {

		StringBuilder contents = new StringBuilder()
				.append("public static interface ")
				.append(StringUtil.capitalize(propertyName)).append("Step")
				.append(" {\n}");

		return builderType.createType(contents.toString(), nextStep, true,
				progressMonitor);

	}

	private IMethod createBuildMethod(IType sourceType, IMethod constructor,
			IType builderType) throws JavaModelException {

		StringBuilder setterCalls = renderSetterCalls();

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

	private StringBuilder renderSetterCalls() {
		StringBuilder setterCalls = new StringBuilder();

		for (IMethod method : setterMethods) {

			setterCalls.append("\n\tresult.").append(method.getElementName())
					.append("(").append(StringUtil.getPropertyNameFromSetter(method.getElementName()))
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
