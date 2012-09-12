package de.below.bgen.generator.components;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;


public class SetterNamingStrategyTest {

	@Test
	public void testWithoutPrefix() {
		SetterNamingStrategy setterNamingStrategy = SetterNamingStrategy.create(null);
		assertEquals("setter name", "foo", setterNamingStrategy.renderSetterNameFor("foo"));
		assertEquals("setter name", "foo", setterNamingStrategy.renderSetterNameFor("Foo"));
	}
	
	@Test
	public void testWithPrefix() {
		SetterNamingStrategy setterNamingStrategy = SetterNamingStrategy.create("set");
		assertEquals("setter name", "setFoo", setterNamingStrategy.renderSetterNameFor("foo"));
	}

}
