package de.below.bgen.builder.generator.components;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import de.below.bgen.util.CodeGenUtils;

/**
 * Methods for rendering code that performs the actual instantiation of the
 * target type.
 * 
 * @author martin
 * 
 */
public abstract class InstantiationStrategy {

	private final IMethod method;

	private InstantiationStrategy(IMethod method) {
		this.method = method;
	}

	/**
	 * Renders a call of a factory method to instantiate the target type.
	 * 
	 * @param factoryMethod
	 *            The Factory method to call.
	 */
	public static InstantiationStrategy factoryMethodCall(final IMethod factoryMethod) {
		return new InstantiationStrategy(factoryMethod) {

			@Override
			public String renderInstantiation() throws JavaModelException {

				StringBuilder result = new StringBuilder();
				result.append(factoryMethod.getDeclaringType().getElementName());
				result.append('.');
				result.append(factoryMethod.getElementName());

				CodeGenUtils.renderArgumentList(factoryMethod, result);
				return result.toString();

			}

		};
	}

	/**
	 * Renders a constructor call to instantiate the target type.
	 * 
	 * @param constructor
	 *            The constructor to call.
	 */
	public static InstantiationStrategy constructorCall(
			final IMethod constructor) {
		return new InstantiationStrategy(constructor) {

			@Override
			public String renderInstantiation() throws JavaModelException {

				StringBuilder result = new StringBuilder("new ");
				result.append(constructor.getDeclaringType().getElementName());

				CodeGenUtils.renderArgumentList(constructor, result);
				return result.toString();

			}

		};
	}

	/**
	 * Renders a call to the implicit default constructor of a class (in case a
	 * class does not have any constructors at all, it implicitly has a public
	 * non-argument-constructor)
	 * 
	 * @param type The type to instantiate.
	 */
	public static InstantiationStrategy defaultConstructorCall(
			final IType type) {
		return new InstantiationStrategy(null) {
			
			@Override
			public String renderInstantiation() throws JavaModelException {
				return String.format("new %s()", type.getElementName());
			}
		};
	}

	public IMethod getMethod() {
		return method;
	}

	public abstract String renderInstantiation() throws JavaModelException;

	
	
}
