package de.below.bgen.generator.components;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

public class FieldGenerationStrategyTest {

	@Test
	public void renderMutableField() {
		String field = FieldGenerationStrategy.PRIVATE_MUTABLE.renderFieldCode("foo", "String");
		assertEquals("field", "private String foo;", field);
	}
	
	@Test
	public void renderImmutableField() {
		String field = FieldGenerationStrategy.PRIVATE_IMMUTABLE.renderFieldCode("foo", "String");
		assertEquals("field", "private final String foo;", field);
	}
	
}
