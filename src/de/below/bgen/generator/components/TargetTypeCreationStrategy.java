package de.below.bgen.generator.components;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.text.edits.TextEdit;

/**
 * Creates an empty {@link IType} for the builder.
 * @author martin below
 */
public abstract class TargetTypeCreationStrategy {

	private static final String CLASS_BODY = "public class %s {\n}";
	private static final String STATIC_CLASS_BODY = "public static class %s {\n}";

	/**
	 * Creates the builder as a static nested class inside a given type.
	 * 
	 * @author martin
	 */
	public static final class NestedTargetTypeStrategy extends
			TargetTypeCreationStrategy {

		private final IType enclosingType;

		public NestedTargetTypeStrategy(IType enclosingType) {
			this.enclosingType = enclosingType;
		}

		@Override
		public IType createBuilderType(IProgressMonitor pm, IType sourceType,
				String builderName) throws JavaModelException {

			return enclosingType.createType(String.format(STATIC_CLASS_BODY, builderName), null,
					true, pm);

		}

		@Override
		protected ISourceRange getTargetRange(IType targetType)
				throws JavaModelException {

			return targetType.getSourceRange();
		}

	}

	/**
	 * Creates the builder as a toplevel-class in a separate compilation unit.
	 * @author martin below
	 */
	public static final class TopLevelTargetTypeStrategy extends
			TargetTypeCreationStrategy {

		private final String packageName;
		private final IPackageFragmentRoot sourceFolder;

		public TopLevelTargetTypeStrategy(IPackageFragmentRoot sourceFolder,
				String packageName) {
			this.sourceFolder = sourceFolder;
			this.packageName = packageName;
		}

		@Override
		public IType createBuilderType(IProgressMonitor pm, IType sourceType,
				String builderName) throws JavaModelException {

			IPackageFragment packageFragment = sourceFolder
					.createPackageFragment(packageName, true, pm);

			packageFragment.makeConsistent(pm);

			ICompilationUnit compilationUnit = packageFragment
					.createCompilationUnit(builderName + ".java", "", true, pm);

			ICompilationUnit sourceCu = sourceType.getCompilationUnit();

			IType result = compilationUnit.createType(String.format(CLASS_BODY, builderName),
					null, true, pm);

			if (sourceCu != null) {
				IImportDeclaration[] imports = sourceCu.getImports();

				for (IImportDeclaration importDeclaration : imports) {
					compilationUnit.createImport(
							importDeclaration.getElementName(), null, pm);
				}
			}

			compilationUnit.createPackageDeclaration(packageName, pm);
			
			if (!sourceType.getPackageFragment().equals(result.getPackageFragment())) {
				compilationUnit.createImport(sourceType.getFullyQualifiedName(), null, pm);
			}
			
			return result;

		}

		@Override
		protected ISourceRange getTargetRange(IType targetType)
				throws JavaModelException {

			return targetType.getCompilationUnit().getSourceRange();
		}

	}

	private TargetTypeCreationStrategy() {
	}


	/**
	 * Creates a new {@link IType} for the given builder. If the type already
	 * exists, it will be overwritten without any further notice.
	 * 
	 * @param pm
	 * @param sourceType
	 *            The class for which a builder should be created. This is
	 *            needed here to obtain the required import statements from.
	 * @param builderName Name of the builder class.
	 * @throws JavaModelException
	 */
	public abstract IType createBuilderType(IProgressMonitor pm,
			IType sourceType, String builderName) throws JavaModelException;

	/**
	 * Formats the generated source code.
	 * 
	 * @param targetType
	 *            The generated builder class.
	 */
	public void format(IType targetType) throws JavaModelException {

		ICompilationUnit compilationUnit = targetType.getCompilationUnit();
		compilationUnit.reconcile(AST.JLS3, true, null, null);

		ISourceRange range = getTargetRange(targetType);

		CodeFormatter formatter = ToolFactory.createCodeFormatter(null);

		TextEdit edit = formatter.format(CodeFormatter.K_COMPILATION_UNIT,
				compilationUnit.getSource(), range.getOffset(),
				range.getLength(), 0, null);

		compilationUnit.applyTextEdit(edit, null);
		compilationUnit.reconcile(AST.JLS3, true, null, null);

	}

	protected abstract ISourceRange getTargetRange(IType targetType)
			throws JavaModelException;

}
