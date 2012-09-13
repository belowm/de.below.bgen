package de.below.codegen;

import static de.below.bgen.TestUtils.assertCodeEquals;

import org.junit.Test;

import de.below.bgen.codegen.Visibility;

public class ConstructorBuilderTest {

	@Test
	public void createNoArgsConstructor() {
		
		String constructor = ConstructorBuilder
			.newConstructor("HansPeter")
				.visibility(Visibility.PUBLIC)
				.noArguments()
			.endConstructor()
			.render()
			;
		
		assertCodeEquals("constructor code", "public HansPeter() { }", constructor);
	}

	@Test
	public void createConstructorWithOneArgument() {
		
		String constructor = ConstructorBuilder
			.newConstructor("HansPeter")
				.visibility(Visibility.PUBLIC)
				.withArguments()
					.argument("String", "name")
				.endArguments()
			.endConstructor()
			.render()
			;
		
		assertCodeEquals("constructor code", "public HansPeter(String name) { }", constructor);
	}
	
	
}
