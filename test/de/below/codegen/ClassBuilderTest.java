package de.below.codegen;

import static de.below.bgen.TestUtils.assertCodeEquals;

import org.junit.Test;

import de.below.bgen.codegen.Visibility;

public class ClassBuilderTest {

	@Test
	public void renderEmptyClass() {
		
		String content = ClassBuilder
			.newClass()
				.visibility(Visibility.PUBLIC)
				.staticClass(true)
				.finalClass()
				.name("Hans")
			.endClass()
			.render();
		;
		
		assertCodeEquals("class sourceode", "public static final class Hans { }", content);
		
	}
	
	@Test
	public void renderNestedClass() {
		
		String content = ClassBuilder
			.newClass()
				.visibility(Visibility.PUBLIC)
				.name("Hans")
				.beginNestedClass()
					.visibility(Visibility.PRIVATE)
					.name("Peter")
				.endClass()
			.endClass()
			.render();
		;
		
		assertCodeEquals("class sourceode", "public class Hans { private class Peter { } }", content);
		
	}
		
	@Test
	public void renderClassWithMethod() {
		
		String content = ClassBuilder
			.newClass()
				.visibility(Visibility.PUBLIC)
				.staticClass(true)
				.finalClass()
				.name("Hans")
				.beginMethod()
					.visibility(Visibility.PUBLIC)
					.voidMethod()
					.name("helloWorld")
				.endMethod()
			.endClass()
		.render();
		;
		
		assertCodeEquals("class sourceode", "public static final class Hans { public void helloWorld() { } }", content);
		
	}
	
	
}
