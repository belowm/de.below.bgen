package de.below.bgen.generator.components;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import de.below.bgen.TestUtils;


public class SetterGenerationStrategyTest {

	@Test
	public void testJavaBeanSetter() {

		String setterCode = SetterGenerationStrategy.JAVA_BEAN.renderSetterCode(
				SetterNamingStrategy.JAVA_BEAN_SETTER_NAMING, TestUtils.createMockType("PersonBuilder", true),
				"age", "Date");
		
		assertEquals("setter code", "public void setAge(Date value) {\n" + 
				"	this.age = value;\n" + 
				"}", setterCode);
		
	}
	
	@Test
	public void testFluentSetter() {
		
		String setterCode = SetterGenerationStrategy.FLUENT.renderSetterCode(
				SetterNamingStrategy.JAVA_BEAN_SETTER_NAMING, TestUtils.createMockType("PersonBuilder", true),
				"age", "Date");
		
		assertEquals("setter code", "public PersonBuilder setAge(Date value) {\n" + 
				"	this.age = value;\n" +
				"	return this;\n" + 
				"}", setterCode);
		
	}
	
}
