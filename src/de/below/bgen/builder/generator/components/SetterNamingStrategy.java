package de.below.bgen.builder.generator.components;

import de.below.bgen.util.CodeGenUtils;

/**
 * Methods for rendering "setter"-methods for a given property.
 * 
 * @author martin
 */
public abstract class SetterNamingStrategy {

	/**
	 * This SetterNamingStrategy creates setter methods that follow the java
	 * bean-conventions. The name of the method constists of the prefix "set",
	 * followed by the name of the property.
	 * 
	 * <p>
	 * A property <code>foo</code> will result in <code>setFoo</code>
	 * </p>
	 * 
	 */
	public static final SetterNamingStrategy JAVA_BEAN_SETTER_NAMING = create("set");

	/**
	 * Creates setter methods that carry the same name as the property.
	 */
	public static final SetterNamingStrategy NON_PREFIXED_SETTER_NAMING = new SetterNamingStrategy() {

		@Override
		public String renderSetterNameFor(String propertyName) {
			return CodeGenUtils.decapitalize(propertyName);
		}
	};

	/**
	 * Creates a new {@link SetterNamingStrategy} for the given prefix.
	 */
	public static SetterNamingStrategy create(String prefix) {
		return prefix == null || prefix.isEmpty() ? NON_PREFIXED_SETTER_NAMING
				: new PrefixedSetterNamingStrategy(prefix);
	}

	/**
	 * Creates a for a setter method for the given property
	 * 
	 * @param propertyName
	 *            The property to generate a setter name for. must not be
	 *            <code>null</code>.
	 */
	public abstract String renderSetterNameFor(String propertyName);

	private final static class PrefixedSetterNamingStrategy extends
			SetterNamingStrategy {

		private final String prefix;

		public PrefixedSetterNamingStrategy(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public String renderSetterNameFor(String propertyName) {
			return prefix + CodeGenUtils.capitalize(propertyName);
		}

	}

}
