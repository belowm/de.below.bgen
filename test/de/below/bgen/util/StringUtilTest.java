package de.below.bgen.util;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;


public class StringUtilTest {

	@Test
	public void capitalize() {
		assertEquals("string", "Foo", StringUtil.capitalize("foo"));
		assertEquals("string", "F", StringUtil.capitalize("f"));
		assertEquals("string", "Foo", StringUtil.capitalize("foo"));
	}	
	
	@Test
	public void decapitalize() {
		assertEquals("string", "foo", StringUtil.decapitalize("foo"));
		assertEquals("string", "f", StringUtil.decapitalize("F"));
		assertEquals("string", "foo", StringUtil.decapitalize("foo"));
	}
}
