package de.below.codegen;

import static de.below.bgen.TestUtils.assertCodeEquals;

import org.junit.Test;

import de.below.bgen.codegen.Visibility;


public class MethodBuilderTest {

	@Test
	public void createNoArgsPublicMethod() {
		
		String method = MethodBuilder.newMethod()
			.visibility(Visibility.PUBLIC)
			.returnType("String")
			.name("execute")
			.endMethod()
			.render()
			;
		
		assertCodeEquals("method source code", "public String execute() { }", method);
		
	}
	
	@Test
	public void createMethodWithOneArgument() {
		
		String method = MethodBuilder.newMethod()
			.visibility(Visibility.PUBLIC)
			.returnType("String")
			.argument("String", "foo")
			.argument("String", "bar")
			.name("execute")
			.endMethod()
			.render()
			;
		
		assertCodeEquals("method source code", "public String execute(String foo, String bar) { }", method);
		
	}

	@Test
	public void createMethodWithBody() {
		
		String method = MethodBuilder.newMethod()
			.visibility(Visibility.PUBLIC)
			.returnType("String")
			.argument("String", "foo")
			.name("setFoo")
			.addStatement("this.foo = foo;")
			.endMethod()
			.render()
			;
		
		assertCodeEquals("method source code", "public String setFoo(String foo) { this.foo = foo; }", method);
		
	}
	
	@Test
	public void createPrivateStaticMethod() {
		
		String method = MethodBuilder.newMethod()
			.visibility(Visibility.PRIVATE)
			.staticMethod()
			.voidMethod()
			.name("setFoo")
		.endMethod()
		.render()
		;
		
		assertCodeEquals("method source code", "private static void setFoo() {  }", method);
		
	}
	
	@Test
	public void createFinalStaticMethod() {
		
		String method = MethodBuilder.newMethod()
			.visibility(Visibility.PUBLIC)
			.finalMethod()
			.voidMethod()
			.name("setFoo")
		.endMethod()
		.render()
		;
		
		assertCodeEquals("method source code", "public final void setFoo() {  }", method);
		
	}
	
}
