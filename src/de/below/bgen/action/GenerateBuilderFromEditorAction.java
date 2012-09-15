package de.below.bgen.action;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import de.below.bgen.Activator;
import de.below.bgen.wizard.NewBuilderWizard;

public class GenerateBuilderFromEditorAction extends Action implements IEditorActionDelegate, IWorkbenchWindowActionDelegate {

	private IEditorPart editor;

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
		
		IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
		
		IEditorInput editorInput = editor.getEditorInput();
		try {
			manager.connect(editorInput);
			ICompilationUnit workingCopy = manager.getWorkingCopy(editorInput);
			
			IType type = workingCopy.getTypes()[0];
			
			NewBuilderWizard wizard = new NewBuilderWizard(type);
			
			wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(type));
			
			WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
			dialog.open();
			
//			CreateDialog dialog = new CreateDialog(new Shell(), new BuilderGenerator());
//			dialog.show(workingCopy);


		} 
		catch (Exception e) {
			Activator.getDefault().log("error opening wizard", e);
		}
		finally {
			manager.disconnect(editorInput);
		}

	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = targetEditor;
	}

    @Override
	public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
	public void init(IWorkbenchWindow window) {
        editor = window.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    }

}