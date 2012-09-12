package de.below.bgen.generator;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import de.below.bgen.generator.components.FieldGenerationStrategy;
import de.below.bgen.generator.components.InstantiationStrategy;
import de.below.bgen.generator.components.SetterGenerationStrategy;
import de.below.bgen.generator.components.SetterNamingStrategy;
import de.below.bgen.generator.components.TargetTypeCreationStrategy;

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

	public BuilderGenerator(IProgressMonitor pm, SetterGenerationStrategy setterGenerator,
			FieldGenerationStrategy fieldGenerator,
			SetterNamingStrategy setterNaming, TargetTypeCreationStrategy target, InstantiationStrategy instantiationStrategy) {
		this.progressMonitor = pm;
		this.setterGenerator = setterGenerator;
		this.fieldGenerator = fieldGenerator;
		this.setterNaming = setterNaming;
		this.target = target;
		this.instantiationStrategy = instantiationStrategy;
	}

	/**
	 * Creates a builder for the given source type in the given target type.
	 * @param sourceType 
	 * @param setterMethods 
	 * @param builderName
	 * @param constructor
	 * @param targetCompilationUnit
	 * @throws JavaModelException
	 */
	public void generate(IType sourceType, List<IMethod> setterMethods,
			String builderName)
			throws JavaModelException {

		this.setterMethods = setterMethods;
		
		if (sourceType == null) {
			throw new IllegalArgumentException("no source type given");
		}
		
		IType builderType = target.createBuilderType(progressMonitor, sourceType, builderName);

		createFactoryMethod(sourceType, builderType);
		
		createSettersAndFields(instantiationStrategy.getMethod(), builderType, builderName);
		
		createSettersAndFieldsFromSetterMethods(setterMethods, builderType, builderName);
		
		createBuildMethod(sourceType, instantiationStrategy.getMethod(), builderType);

		createBuildUponMethod(sourceType, builderType);
		
		
		format(builderType);
		
	}


	private void createBuildUponMethod(IType sourceType, IType builderType) 
	throws JavaModelException {

		StringBuilder copyStatements = new StringBuilder();
		
		for (IMethod method : sourceType.getMethods()) {
			
			String methodName = method.getElementName();
			if (methodName.startsWith("get") && methodName.length() > 3) {
				
				String setterMethodName = 
					setterNaming.renderSetterNameFor(methodName.substring(3));
				
				IMethod setterMethod = builderType.getMethod(
						setterMethodName, new String[] { method.getReturnType() });
				
				if (setterMethod.exists()) {
					copyStatements.append("\tbuilder.").append(setterMethod.getElementName());
					copyStatements.append("(original.").append(method.getElementName()).append("());\n");
				}
			}
		}
		
		if (copyStatements.length() > 0) {
			String contents = new StringBuilder()
				.append("public static ")
				.append(builderType.getElementName()).append(" buildUpon(")
				.append(sourceType.getElementName())
				.append(" original) {\n")
				.append("\t").append(builderType.getElementName())
				.append(" builder = newBuilder();\n")
				.append(copyStatements)
				.append("\treturn builder;")
				.append("\n}")
				.toString()
				;
			
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

	public void createSettersAndFields(IMethod method, IType builderType, String builderName)
			throws JavaModelException {

		if (method == null)
			return;
		
		for (int i = 0; i < method.getParameterNames().length; i++) {

			String paramName = method.getParameterNames()[i];
			String paramType = Signature.toString(method
					.getParameterTypes()[i]);

			createSetterAndField(builderType, builderName,
					paramName, paramType);

		}

	}
	
	
	private void createSettersAndFieldsFromSetterMethods(
			List<IMethod> setterMethods, IType builderType, String builderName) throws JavaModelException {

		for (IMethod setter : setterMethods) {
			
			createSetterAndField(builderType, builderName,
					getPropertyNameFromSetter(setter),
					Signature.toString(setter.getParameterTypes()[0]));
			
		}
		
	}

	public void createSettersAndFieldsFromConstructor(IMethod constructor, IType type, String builderName)
			throws JavaModelException {

		for (int i = 0; i < constructor.getParameterNames().length; i++) {

			String paramName = constructor.getParameterNames()[i];
			String paramType = Signature.toString(constructor
					.getParameterTypes()[i]);

			createSetterAndField(type, builderName, paramName, paramType);

		}
	}

	private void createSetterAndField(IType type, String builderName,
			String paramName, String paramType) throws JavaModelException {
		type.createField(
				fieldGenerator.renderFieldCode(paramName, paramType), null,
				true, progressMonitor);

		String setterCode = setterGenerator.renderSetterCode(setterNaming,
				builderName, paramName, paramType);
		System.out.println(setterCode);
		type.createMethod(setterCode, null, true, progressMonitor);
	}

	private IMethod createBuildMethod(IType sourceType, IMethod constructor,
			IType builderType) throws JavaModelException {

		
		StringBuilder setterCalls = renderSetterCalls();

		String exceptions = renderExceptions(constructor);

		String instantiation = instantiationStrategy.renderInstantiation();
		
		String buildMethodSource = String.format(BUILD_METHOD,
				sourceType.getElementName(), instantiation, setterCalls, exceptions);


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
				}
				else {
					exceptions.append(", ");
				}
				
				exceptions.append(exception.substring(1, exception.length() - 1));
			}
		}
		return exceptions.toString();
	}

	private StringBuilder renderSetterCalls() {
		StringBuilder setterCalls = new StringBuilder();
		
		for (IMethod method : setterMethods) {
			
			setterCalls.append("\n\tresult.").append(method.getElementName())
					.append("(")
					.append(getPropertyNameFromSetter(method))
					.append(");");
			
		}
		
		return setterCalls;
	}

	private String getPropertyNameFromSetter(IMethod method) {
		return decapitalize(method.getElementName().substring(3));
	}

	private String decapitalize(String str) {
		
		if (str.length() < 2) {
			return str;
		}
		
		return Character.toLowerCase(str.charAt(0)) + str.substring(1);
	}

	private void format(IType builderType) throws JavaModelException {
		target.format(builderType);
	}

	private void createFactoryMethod(IType source, IType builder) throws JavaModelException {
		
		IType staticMethodHome = getStaticMethodHome(source, builder);
		
		staticMethodHome.createMethod(
				String.format(FACTORY_METHOD, builder.getElementName()), null,
				true, progressMonitor);
		
	}

}
