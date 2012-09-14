package de.below.bgen.builder.generator;

import static de.below.bgen.builder.generator.Expressions.*;
import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import de.below.bgen.codegen.Expression;
import de.below.codegen.JavaCodeWriter;

public class ExpressionsTest {

	@Test
	public void renderStaticMethodCall() {
		Expression call = methodCall(type("Calendar"), "getInstance", numberLiteral(5));
		assertEquals("code", "Calendar.getInstance(5)", render(call));
	}
	
	@Test
	public void renderMethodCallWithStringLiteral() {
		Expression expr = methodCall(object("person"), "setFirstName", stringLiteral("test"));
		assertEquals("code", "person.setFirstName(\"test\")", render(expr));
	}

	@Test
	public void renderAssignment() {
		Expression expr = assignment(variable("foo"), stringLiteral("bar"));
		assertEquals("code", "foo = \"bar\"", render(expr));
	}
	
	@Test
	public void renderAssignmentWithDeclaration() {
		Expression expr = declarationWithAssignment(type("String"), variable("foo"), stringLiteral("bar"));
		assertEquals("code", "String foo = \"bar\"", render(expr));
	}
	
	@Test
	public void renderReturnStatement() {
		Expression expr = returnStatement(variable("bar"));
		assertEquals("code", "return bar", render(expr));
	}
	
	@Test
	public void renderInstantiation() {
		Expression expr = instantiation(type("Date"), numberLiteral(550));
		assertEquals("code", "new Date(550)", render(expr));
	}
	

	private String render(Expression call) {
		JavaCodeWriter writer = JavaCodeWriter.create();
		call.render(writer);
		return writer.toString();
	}
	
}
