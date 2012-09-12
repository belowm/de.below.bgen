package de.below.bgen.generator.components;

import org.eclipse.jdt.core.IType;

/**
 * Methods for generating "setter" methods.
 * 
 * @author martin
 * 
 */
public abstract class SetterGenerationStrategy {

	private static final String FLUENT_SETTER = "public %s %s(%s value) {\n\tthis.%s = value;\n\treturn this;\n}";

	private static final String STANDARD_SETTER = "public void %s(%s value) {\n\tthis.%s = value;\n}";

	/**
	 * Creates setter methods with a void return type.
	 */
	public final static SetterGenerationStrategy JAVA_BEAN = new SetterGenerationStrategy() {

		@Override
		public String renderSetterCode(SetterNamingStrategy namingStrategy,
				IType returnType, String paramName, String paramType) {

			return String.format(STANDARD_SETTER,
					namingStrategy.renderSetterNameFor(paramName), paramType,
					paramName);
		}

	};

	/**
	 * Creates setter methods that return the object on which the setter was
	 * called. Useful for chaining a sequence of calls.
	 */
	public final static SetterGenerationStrategy FLUENT = new SetterGenerationStrategy() {

		@Override
		public String renderSetterCode(SetterNamingStrategy namingStrategy,
				IType returnType, String paramName, String paramType) {

			return String.format(FLUENT_SETTER, returnType.getElementName(),
					namingStrategy.renderSetterNameFor(paramName), paramType,
					paramName);
		}

	};

	/**
	 * Renders a setter-method for the given property using the given naming
	 * strategy.
	 * 
	 * @param namingStrategy
	 *            Strategy to render the method name.
	 * @param returnTypeName
	 *            The name of the builder class.
	 * @param propertyName
	 *            The name of the property.
	 * @param propertyType
	 *            The type of the property.
	 */
	public abstract String renderSetterCode(
			SetterNamingStrategy namingStrategy, IType returnType,
			String propertyName, String propertyType);

}
