package de.below.bgen.builder.generator.components;

/**
 * Methods for rendering private fields.
 * @author martin
 */
public abstract class FieldGenerationStrategy {

	private static final String MUTABLE_PRIVATE_FIELD = "private %s %s;";
	private static final String IMMUTABLE_PRIVATE_FIELD = "private final %s %s;";

	/**
	 * Renders a private field that is not final.
	 */
	public static final FieldGenerationStrategy PRIVATE_MUTABLE = new FieldGenerationStrategy(
			MUTABLE_PRIVATE_FIELD) {
	};
	public static final FieldGenerationStrategy PRIVATE_IMMUTABLE = new FieldGenerationStrategy(
			IMMUTABLE_PRIVATE_FIELD) {
	};

	private final String template;

	private FieldGenerationStrategy(String template) {
		this.template = template;
	}

	public String renderFieldCode(String paramName, String paramType) {
		return String.format(template, paramType, paramName);
	}

}
