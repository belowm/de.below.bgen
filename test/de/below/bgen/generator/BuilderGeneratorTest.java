package de.below.bgen.generator;

import static junit.framework.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.junit.Before;
import org.junit.Test;

import de.below.bgen.builder.generator.BuilderGeneratorFacade;

public class BuilderGeneratorTest extends AbstractWorkspaceAwareTest {

	private final static String classWithConstructor = 
			"public class Person {\n" + 
			"		\n" + 
			"		private final String firstname, lastname;\n" + 
			"\n" + 
			"		public Person(String firstname, String lastname) {\n" + 
			"			this.firstname = firstname;\n" + 
			"			this.lastname = lastname;\n" + 
			"		}\n" + 
			"\n" + 
			"		public String getFirstname() {\n" + 
			"			return firstname;\n" + 
			"		}\n" + 
			"\n" + 
			"		public String getLastname() {\n" + 
			"			return lastname;\n" + 
			"		}\n" + 
			"		\n" + 
			"	}\n" + 
			"";

	private final static String beanClass = 
		"public class Bean {\n" + 
		"		\n" + 
		"		private String firstname, lastname;\n" + 
		"\n" + 
		"\n" + 
		"		public void setFirstname(String firstname) {}" +
		"		public void setLastname(String lastname) {}" +		
		"		public String getFirstname() {\n" + 
		"			return firstname;\n" + 
		"		}\n" + 
		"\n" + 
		"		public String getLastname() {\n" + 
		"			return lastname;\n" + 
		"		}\n" + 
		"		\n" + 
		"	}\n" + 
		"";

	@Before
	public void setup() {
		generator = BuilderGeneratorFacade.newInstance();
	}
	
	
	@Test
	public void testWithImmutableClass() throws CoreException {
		
		ICompilationUnit cu = getCompilationUnitFor("de.foo.bar", "Person.java", classWithConstructor);
		ICompilationUnit targetUnit = getCompilationUnitFor("de.foo.bar", "PersonBuilder.java", "");
		
		IType type = cu.getType("Person");
		
		assertTrue(type.exists());
		
		
		IMethod constructor = type.getMethod("Person", new String[] { "QString;", "QString;" });
		assertTrue(constructor.exists());
		
		generator.withConstructor(constructor);
		generator.withType(constructor.getDeclaringType());
		generator.withTargetPackageName((IPackageFragmentRoot) targetUnit
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT), "de.foo.bar");
		
		generator.withBuilderName("PersonBuilder");
		generator.generate();
		
		{
			IType builderType = targetUnit.getType("PersonBuilder");
			assertTrue("did not create target type. types created: " + 
					Arrays.toString(targetUnit.getTypes()), builderType.exists());

			IField field1 = builderType.getField("firstname");
			assertTrue("did not create field for argument 'firstname'", field1.exists());
			
			IMethod setter1 = builderType.getMethod("setFirstname", new String[] { "QString;" });
			assertTrue("did not create setter for constructor argument firstname", setter1.exists());
			
			
			assertTrue("did not create builder factory method", builderType.getMethod("newBuilder", new String[] {}).exists());
		}
		
		dump(targetUnit);
		
	}

	@Test
	public void testWithMutableBean() throws CoreException {
		
		ICompilationUnit cu = getCompilationUnitFor("de.hans.peter", "Bean.java", beanClass);
		ICompilationUnit targetUnit = getCompilationUnitFor("de.hans.peter", "BeanBuilder.java", "");
		IType type = cu.getType("Bean");
		IMethod setter = type.getMethod("setFirstname", new String[] { "QString;" });
		assertTrue(setter.exists());
		
		generator.withType(type);
		generator.withSetterProperties(Arrays.asList(setter));
		generator.withTargetPackageName((IPackageFragmentRoot) targetUnit
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT), "de.hans.peter");
		generator.withBuilderName("BeanBuilder");

		generator.generate();

		{
			IType builderType = targetUnit.getType("BeanBuilder");
			assertTrue("did not create target type. types created: " + 
					Arrays.toString(targetUnit.getTypes()), builderType.exists());
			
			IMethod setter1 = builderType.getMethod("setFirstname", new String[] { "QString;" });
			assertTrue("did not create setter for property firstname", setter1.exists());
			
			
			IField field1 = builderType.getField("firstname");
			assertTrue("did not create field for argument 'firstname'", field1.exists());
			
		}
		
	}
	
	@Test
	public void testWithNonPrefixedSetters() throws CoreException {
		
		ICompilationUnit cu = getCompilationUnitFor("de.hans.peter", "Bean.java", beanClass);
		ICompilationUnit targetUnit = getCompilationUnitFor("de.hans.peter", "BeanBuilder.java", "");
		IType type = cu.getType("Bean");
		IMethod setter = type.getMethod("setFirstname", new String[] { "QString;" });
		assertTrue(setter.exists());
		
		generator.withType(type);
		generator.withSetterProperties(Arrays.asList(setter));
		generator.withTargetPackageName((IPackageFragmentRoot) targetUnit
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT), "de.hans.peter");
		generator.withBuilderName("BeanBuilder");
		generator.withSetterPrefix(null);
		generator.generate();

		{
			IType builderType = targetUnit.getType("BeanBuilder");
			assertTrue("did not create target type. types created: " + 
					Arrays.toString(targetUnit.getTypes()), builderType.exists());
			
			IMethod setter1 = builderType.getMethod("firstname", new String[] { "QString;" });
			assertTrue("did not create setter for property firstname", setter1.exists());
			
		}
	}

	@Test
	public void mustCreateBuildUponMethod() throws CoreException {
		
		ICompilationUnit cu = getCompilationUnitFor("de.hans.peter", "Bean.java", beanClass);
		ICompilationUnit targetUnit = getCompilationUnitFor("de.hans.peter", "BeanBuilder.java", "");
		IType type = cu.getType("Bean");
		IMethod setter = type.getMethod("setFirstname", new String[] { "QString;" });
		assertTrue(setter.exists());
		
		generator.withType(type);
		generator.withSetterProperties(Arrays.asList(setter));
		generator.withTargetPackageName((IPackageFragmentRoot) targetUnit
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT), "de.hans.peter");
		generator.withBuilderName("BeanBuilder");
		generator.withSetterPrefix(null);
		generator.generate();

		{
			IType builderType = targetUnit.getType("BeanBuilder");
			
			IMethod buildUponMethod = 
				builderType.getMethod("buildUpon", new String[] { "QBean;" });
			
			assertTrue("did not create buildUponMethod", buildUponMethod.exists());
			
		}
	}
	
	@Test
	public void mustCreateFactoryMethod() throws CoreException {
		
		ICompilationUnit cu = getCompilationUnitFor("de.hans.peter", "Bean.java", beanClass);
		ICompilationUnit targetUnit = getCompilationUnitFor("de.hans.peter", "BeanBuilder.java", "");
		IType type = cu.getType("Bean");
		
		generator.withType(type);
		generator.withTargetPackageName((IPackageFragmentRoot) targetUnit
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT), "de.hans.peter");
		generator.withBuilderName("BeanBuilder");
		generator.withSetterPrefix(null);
		generator.generate();

		{
			IType builderType = targetUnit.getType("BeanBuilder");
			
			IMethod factoryMethod = 
				builderType.getMethod("newBuilder", new String[] {  });
			
			assertTrue("did not create factory-method", factoryMethod.exists());
			assertTrue("factory method not static", (factoryMethod.getFlags() & Flags.AccStatic) > 0);
			
		}
	}

}
