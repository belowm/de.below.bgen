package de.below.bgen.bean.generator;

import static de.below.bgen.TestUtils.assertConstructorExists;
import static de.below.bgen.TestUtils.assertFieldExists;
import static de.below.bgen.TestUtils.assertMethodExists;
import static de.below.bgen.TestUtils.assertReturnType;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.junit.Test;

import de.below.bgen.builder.generator.components.TargetTypeCreationStrategy.TopLevelTargetTypeStrategy;
import de.below.bgen.generator.AbstractWorkspaceAwareTest;

public class BeanGeneratorTest extends AbstractWorkspaceAwareTest {

	private static final String STRING_TYPE = "QString;";
	private static final String DATE_TYPE = "QDate;";
	
	/*@formatter:off*/
	private final static String personInterfaceSource = 
		"public interface Person {\n" + 
		"	\n" + 
		"	public String getFirstname();\n" + 
		"	public String getLastname();\n" + 
		"	public Date getBirthday();\n" + 
		"	\n" + 
		"}\n";

	/*@formatter:on*/

	@Test
	public void createMutableType() throws CoreException {

		ICompilationUnit personInterface = getCompilationUnitFor("com.foo.bar",
				"Person.java", personInterfaceSource);

		IType type = personInterface.getType("Person");
		assertNotNull(type);

		IPackageFragmentRoot sourceFolder = (IPackageFragmentRoot) personInterface
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		TopLevelTargetTypeStrategy targetTypeGenerator = new TopLevelTargetTypeStrategy(
				sourceFolder, "com.foo.bar");

		BeanGenerator generator = new BeanGenerator(type, "PersonImpl",
				PROGRESS_MONITOR, targetTypeGenerator, true);

		IType implementation = generator.generate();

		dump(implementation.getCompilationUnit());

		assertTrue("did not create the implementation class",
				implementation != null && implementation.exists());

		assertFieldExists(implementation, "firstname");
		assertMethodExists(implementation, "getFirstname");
		assertReturnType(implementation.getMethod("getFirstname", args()), STRING_TYPE);
		assertMethodExists(implementation, "setFirstname", STRING_TYPE);

	}
	
	@Test
	public void createImmutableType() throws CoreException {

		ICompilationUnit personInterface = getCompilationUnitFor("com.foo.bar",
				"Person.java", personInterfaceSource);

		IType type = personInterface.getType("Person");
		assertNotNull(type);

		IPackageFragmentRoot sourceFolder = (IPackageFragmentRoot) personInterface
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		TopLevelTargetTypeStrategy targetTypeGenerator = new TopLevelTargetTypeStrategy(
				sourceFolder, "com.foo.bar");

		BeanGenerator generator = new BeanGenerator(type, "PersonImpl",
				PROGRESS_MONITOR, targetTypeGenerator, false);

		IType implementation = generator.generate();

		dump(implementation.getCompilationUnit());

		assertTrue("did not create the implementation class",
				implementation != null && implementation.exists());

		assertFieldExists(implementation, "firstname");
		assertMethodExists(implementation, "getFirstname");
		
		assertFalse("must not create setter for immutable type", implementation
				.getMethod("setFirstname", args(STRING_TYPE))
				.exists());
		
		assertConstructorExists(implementation, STRING_TYPE, STRING_TYPE, DATE_TYPE);

	}

	private String[] args(String... args) {
		return args;
	}	

}
