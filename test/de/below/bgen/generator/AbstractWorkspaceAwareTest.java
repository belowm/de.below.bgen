package de.below.bgen.generator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.junit.Before;

import de.below.bgen.builder.generator.BuilderGeneratorFacade;

/**
 * Base class for tests that require a workspace and a project.
 * 
 * <p>
 * Note that tests derived from this class need to be run as eclipse plugin
 * test!
 * </p>
 * 
 * @author martin
 * 
 */
public abstract class AbstractWorkspaceAwareTest {

	protected static final NullProgressMonitor PROGRESS_MONITOR = new NullProgressMonitor();

	protected IProject project;
	protected IJavaProject jProject;
	protected IFolder sourceFolder;
	protected BuilderGeneratorFacade generator;

	@Before
	public final void setupEnvironment() throws CoreException {

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

		IClasspathEntry newSourceEntry = JavaCore.newSourceEntry(sourceFolder
				.getFullPath());

		jProject.setRawClasspath(new IClasspathEntry[] { newSourceEntry },
				PROGRESS_MONITOR);

	}
	
	protected ICompilationUnit getCompilationUnitFor(String packageName, String cuName, String source) throws CoreException {

		IPackageFragmentRoot fragmentRoot = jProject.getPackageFragmentRoot(sourceFolder);
		IPackageFragment fragment = fragmentRoot.createPackageFragment(packageName, true, PROGRESS_MONITOR);
		
		return fragment.createCompilationUnit(cuName, source, true, PROGRESS_MONITOR);
	}

	public void dump(ICompilationUnit targetUnit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(targetUnit);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		ASTNode ast = parser.createAST(null);
		System.out.println(ast);
	}


}
