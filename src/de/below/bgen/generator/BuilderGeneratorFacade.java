package de.below.bgen.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import de.below.bgen.generator.components.FieldGenerationStrategy;
import de.below.bgen.generator.components.InstantiationStrategy;
import de.below.bgen.generator.components.SetterGenerationStrategy;
import de.below.bgen.generator.components.SetterNamingStrategy;
import de.below.bgen.generator.components.TargetTypeCreationStrategy;

/**
 * "Fluent API" for setting up a {@link BuilderGenerator}.
 * @author martin
 */
public class BuilderGeneratorFacade {

	private SetterGenerationStrategy setterGenerator = SetterGenerationStrategy.FLUENT;
	private final FieldGenerationStrategy fieldGenerationStrategy = FieldGenerationStrategy.PRIVATE_MUTABLE;
	private SetterNamingStrategy setterNamingStrategy = SetterNamingStrategy.JAVA_BEAN_SETTER_NAMING;
	private String builderName;
	private IProgressMonitor progressMonitor = new NullProgressMonitor();
	private List<IMethod> setterProperties = Collections.emptyList();
	private IType type;
	private TargetTypeCreationStrategy targetTypeCreationStrategy;
	
	private InstantiationStrategy instantiationStrategy;

	public static BuilderGeneratorFacade newInstance() {
		return new BuilderGeneratorFacade();
	}

	private BuilderGeneratorFacade() {

	}

	/**
	 * Configures the generator to generate fluent setters, meaning that each
	 * setter-method will return the builder itself. This is useful for chaining
	 * a sequence of calls to the builder.
	 * 
	 * @param generateFluentSetters
	 * @return {@link BuilderGeneratorFacade this}
	 */
	public BuilderGeneratorFacade withFluentSetters(
			boolean generateFluentSetters) {
		setterGenerator = generateFluentSetters ? SetterGenerationStrategy.FLUENT
				: SetterGenerationStrategy.JAVA_BEAN;
		return this;
	}

	/**
	 * Sets the prefix for the "setter" - methods of the builder. If
	 * <code>null</code> or an empty String is given, no prefix will be used.
	 * 
	 * @param prefix
	 *            The prefix to use.
	 * @return {@link BuilderGeneratorFacade this}
	 */
	public BuilderGeneratorFacade withSetterPrefix(String prefix) {
		setterNamingStrategy = SetterNamingStrategy.create(prefix);
		return this;
	}

	/**
	 * Uses the given monitor to track progress.
	 * 
	 * @return {@link BuilderGeneratorFacade this}
	 */
	public BuilderGeneratorFacade withProgressMonitor(IProgressMonitor pm) {
		this.progressMonitor = pm;
		return this;
	}

	public BuilderGeneratorFacade withBuilderName(String builderName) {
		this.builderName = builderName;
		return this;
	}

	public BuilderGeneratorFacade withEnclosingType(IType enclosingType) {
		
		this.targetTypeCreationStrategy = 
			new TargetTypeCreationStrategy.NestedTargetTypeStrategy(enclosingType);
		
		return this;
	}

	/**
	 * Triggers the generation.
	 * 
	 * @param constructor
	 *            The constructor, from which the properties should be taken.
	 * @param targetCompilationUnit
	 *            The compilation unit where the builder should be nested in.
	 * @throws JavaModelException
	 */
	public void generate()
			throws JavaModelException {

		if (builderName == null) {
			throw new IllegalArgumentException("builder name not set");
		}
		
		if (type == null) {
			throw new IllegalArgumentException("type not set");
		}
		
		if (instantiationStrategy == null) {
			instantiationStrategy = InstantiationStrategy
					.implicitDefaultConstructorCall(type);
		}
		
		BuilderGenerator generator = new BuilderGenerator(progressMonitor, setterGenerator,
				fieldGenerationStrategy, setterNamingStrategy, targetTypeCreationStrategy, instantiationStrategy);

		generator.generate(type, setterProperties, new ArrayList<String>(), builderName);
	}

	/**
	 * Sets the constructor which should be called by the generated builder.
	 * 
	 * The generator will create a field and a setter-method for each
	 * constructor argument.
	 */
	public BuilderGeneratorFacade withConstructor(IMethod constructor) {
		this.instantiationStrategy = InstantiationStrategy.constructorCall(constructor);
		return this;
	}

	public BuilderGeneratorFacade withSetterProperties(List<IMethod> setterProperties) {
		this.setterProperties = setterProperties == null ? Collections
				.<IMethod> emptyList() : setterProperties;
		return this;
	}

	public BuilderGeneratorFacade withType(IType type) {
		this.type = type;
		if (builderName == null) {
			builderName = type.getElementName() + "Builder";
		}
		return this;
	}
	
	public BuilderGeneratorFacade withTargetPackageName(IPackageFragmentRoot sourceFolder, String targetPackage) {
		
		this.targetTypeCreationStrategy = new TargetTypeCreationStrategy.TopLevelTargetTypeStrategy(
				sourceFolder, targetPackage);
		
		return this;
	}

	public BuilderGeneratorFacade withFactoryMethod(IMethod factoryMethod) throws JavaModelException {
		instantiationStrategy = InstantiationStrategy.factoryMethodCall(factoryMethod);
		return this;
	}

}
