package de.below.codegen;

import static de.below.bgen.TestUtils.assertCodeEquals;

import org.junit.Test;

import de.below.bgen.codegen.Visibility;


public class FieldBuilderTest {

	@Test
	public void createField() {
		
		JavaCodeWriter field = FieldBuilder.newField()
			.type("String")
			.name("hans")
			.build();
		
		assertCodeEquals("field declaration", "String hans;", field.render());
		
	}
	
	@Test
	public void createPrivateFinalField() {
		
		JavaCodeWriter field = FieldBuilder.newField()
			.type("String")
			.name("hans")
			.visibility(Visibility.PRIVATE)
			.staticField(true)
			.finalField(true)
			.build();
		
		assertCodeEquals("field declaration", "private final static String hans;", field.render());
		
	}
	
}
