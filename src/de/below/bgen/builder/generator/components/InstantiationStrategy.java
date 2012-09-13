package de.below.bgen.builder.generator.components;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

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

				renderArgumentList(factoryMethod, result);
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

				renderArgumentList(constructor, result);
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
	public static InstantiationStrategy implicitDefaultConstructorCall(
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

	
	protected void renderArgumentList(final IMethod method,
			StringBuilder result) throws JavaModelException {
		result.append("(");
		for (int i = 0; i < method.getParameterNames().length; i++) {

			String paramName = method.getParameterNames()[i];

			if (i > 0) {
				result.append(", ");
			}

			result.append(paramName);

		}
		result.append(")");
	}
	
}
