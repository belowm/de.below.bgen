package de.below.bgen.generator;

import static junit.framework.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.junit.Before;
import org.junit.Test;

import de.below.bgen.builder.generator.BuilderGeneratorFacade;

public class BuilderGeneratorTest {

	private static final NullProgressMonitor PROGRESS_MONITOR = new NullProgressMonitor();
	
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


	
	private IProject project;
	
	private BuilderGeneratorFacade generator;

	private IJavaProject jProject;

	private IFolder sourceFolder;

	@Before
	public void init() throws CoreException {
		
		generator = BuilderGeneratorFacade.newInstance();
		 
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		
		project = workspace.getProject("project1");
		
		if (!project.exists()) {
			project.create(PROGRESS_MONITOR);
		}
		
		if (!project.isOpen()) {
			project.open(PROGRESS_MONITOR);
		}

		if (!project.hasNature(JavaCore.NATURE_ID)) {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = JavaCore.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, PROGRESS_MONITOR);
		}
		
		jProject = JavaCore.create(project);
		
		sourceFolder = project.getFolder("/src");
		
		if (!sourceFolder.exists()) {
			sourceFolder.create(true, true, PROGRESS_MONITOR);
		}

		IClasspathEntry newSourceEntry = JavaCore.newSourceEntry(sourceFolder.getFullPath());
		
		jProject.setRawClasspath(new IClasspathEntry[] { newSourceEntry }, PROGRESS_MONITOR);
		
		
	}
	
	@Test
	public void testWithImmutableClass() throws CoreException {
		
		ICompilationUnit cu = getCompilationUnitFor("de.foo.bar", "Person.java", classWithConstructor);
		ICompilationUnit targetUnit = getCompilationUnitFor("de.foo.bar", "PersonBuilder.java", "");
		
		System.out.println(Arrays.toString(cu.getTypes()));
		
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
			
			IMethod setter1 = builderType.getMethod("setFirstname", new String[] { "QString;" });
			assertTrue("did not create setter for constructor argument firstname", setter1.exists());
			
			IField field1 = builderType.getField("firstname");
			assertTrue("did not create field for argument 'firstname'", field1.exists());
			
			assertTrue("did not create builder factory method", builderType.getMethod("newBuilder", new String[] {}).exists());
		}
		
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

		//dump(targetUnit);

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
	public void mustBuildUponMethod() throws CoreException {
		
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

	public void dump(ICompilationUnit targetUnit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(targetUnit);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		ASTNode ast = parser.createAST(null);
		System.out.println(ast);
	}

	private ICompilationUnit getCompilationUnitFor(String packageName, String cuName, String source) throws CoreException {

		IPackageFragmentRoot fragmentRoot = jProject.getPackageFragmentRoot(sourceFolder);
		IPackageFragment fragment = fragmentRoot.createPackageFragment(packageName, true, PROGRESS_MONITOR);
		
		return fragment.createCompilationUnit(cuName, source, true, PROGRESS_MONITOR);
	}
	
}
