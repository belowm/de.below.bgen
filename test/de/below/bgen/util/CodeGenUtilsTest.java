package de.below.bgen.util;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;


public class CodeGenUtilsTest {

	@Test
	public void capitalize() {
		assertEquals("string", "Foo", CodeGenUtils.capitalize("foo"));
		assertEquals("string", "F", CodeGenUtils.capitalize("f"));
		assertEquals("string", "Foo", CodeGenUtils.capitalize("foo"));
	}	
	
	@Test
	public void decapitalize() {
		assertEquals("string", "foo", CodeGenUtils.decapitalize("foo"));
		assertEquals("string", "f", CodeGenUtils.decapitalize("F"));
		assertEquals("string", "foo", CodeGenUtils.decapitalize("foo"));
	}
}
