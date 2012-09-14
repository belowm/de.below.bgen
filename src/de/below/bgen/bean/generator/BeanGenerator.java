package de.below.bgen.bean.generator;

import static de.below.bgen.builder.generator.Expressions.*;
import static de.below.bgen.util.CodeGenUtils.*;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import de.below.bgen.builder.generator.Expressions;
import de.below.bgen.builder.generator.components.TargetTypeCreationStrategy;
import de.below.bgen.codegen.Visibility;
import de.below.bgen.util.CodeGenUtils;
import de.below.codegen.ClassBuilder;
import de.below.codegen.ClassBuilder.InClassStep;
import de.below.codegen.ConstructorBuilder.ArgsStep;
import de.below.codegen.ConstructorBuilder.InConstructorStep;
import de.below.codegen.JavaCodeWriter;

public class BeanGenerator {

	private final TargetTypeCreationStrategy targetTypeGenerator;
	private final IType interfaceType;
	private final IProgressMonitor pm;
	private final String targetTypeName;
	private final boolean mutable;
	
	public BeanGenerator(IType interfaceType, String targetTypeName,
			IProgressMonitor pm,
			TargetTypeCreationStrategy targetTypeGenerator,
			boolean mutable
			) {
		this.targetTypeName = targetTypeName;
		this.pm = pm;
		this.targetTypeGenerator = targetTypeGenerator;
		this.interfaceType = interfaceType;
		this.mutable = mutable;
	}

	public IType generate() throws JavaModelException {

		InClassStep<JavaCodeWriter> targetType = ClassBuilder.newClass()
			.visibility(Visibility.PUBLIC)
			.name(targetTypeName);
		;
		
		MethodAnalyzer methodAnalyzer = MethodAnalyzer.analyze(interfaceType);
		
		for (IMethod getterMethod : methodAnalyzer.getGetters()) {
			createFieldFor(targetType, getterMethod);
		}
		

		for (IMethod getterMethod : methodAnalyzer.getGetters()) {
			
			createGetterMethod(targetType, getterMethod);
			
			if (mutable) {
				createSetterMethod(targetType, getterMethod);
			}
		}

		if (!mutable) {
			createConstructor(targetType, methodAnalyzer.getGetters());
		}

		
		/*
		for (Property property : properties.getSetterProperties()) {
			String setterCode = setterGenerationStrategy.renderSetterCode(
					SetterNamingStrategy.JAVA_BEAN_SETTER_NAMING, null,
					property.getName(), property.getType());
			
			targetType.createMethod(setterCode, targetType, true, pm);
		}
		*/
		
		//return targetType;
		
		String sourceCode = targetType.endClass().render();
		
		return targetTypeGenerator.createTargetType(pm, interfaceType, targetTypeName, sourceCode);
		
	}

	private void createConstructor(InClassStep<JavaCodeWriter> targetType, List<IMethod> getterMethods) throws JavaModelException {
		
		ArgsStep<InClassStep<JavaCodeWriter>> constructorArguments = 
			targetType.beginConstructor()
				.visibility(Visibility.PUBLIC)
				.withArguments();
			
		
		for (IMethod getter: getterMethods) {
			String propertyName = getPropertyNameFromAccessorMethod(getter.getElementName());
			constructorArguments.argument(Signature.toString(getter.getReturnType()), propertyName);
		}
		
		InConstructorStep<InClassStep<JavaCodeWriter>> constructor = constructorArguments
			.endArguments();
			
		for (IMethod getter: getterMethods) {
			String propertyName = getPropertyNameFromAccessorMethod(getter.getElementName());
			constructor.addStatement("this.", propertyName, "=", propertyName);
		}
		
		constructor.endConstructor();
		
	}

	private void createGetterMethod(InClassStep<JavaCodeWriter> targetType, IMethod getterMethod) 
	throws JavaModelException {

		String propertyName = getPropertyNameFromAccessorMethod(getterMethod.getElementName());
		
		targetType.beginMethod()
			.visibility(Visibility.PUBLIC)
			.returnType(Signature.toString(getterMethod.getReturnType()))
			.name(getterMethod.getElementName())
			.addStatement(returnStatement(variable(propertyName)))
		.endMethod();
			
	}

	private void createSetterMethod(InClassStep<JavaCodeWriter> targetType, IMethod getterMethod)
	throws IllegalArgumentException, JavaModelException {

		String propertyName = decapitalize(getterMethod.getElementName().substring(3));
		
		targetType.beginMethod()
			.visibility(Visibility.PUBLIC)
			.returnType(Signature.toString(getterMethod.getReturnType()))
			.argument(Signature.toString(getterMethod.getReturnType()), propertyName)
			.name("set" + CodeGenUtils.capitalize(propertyName))
			.addStatement(Expressions.assignment(self(), variable(propertyName), variable(propertyName)))
		.endMethod();
		
	}

	private void createFieldFor(InClassStep<JavaCodeWriter> targetType, IMethod getterMethod) 
	throws JavaModelException {
		
		targetType.beginField()
			.type(Signature.toString(getterMethod.getReturnType()))
			.name(decapitalize(getterMethod.getElementName().substring(3)))
			.finalField(!mutable)
			.visibility(Visibility.PRIVATE)
			.buildField()
			;
		
	}
	

}
