package de.below.bgen.action;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import de.below.bgen.Activator;
import de.below.rcp.widgets.SelectionUtil;
import de.below.rcp.widgets.SimpleWizard;

public abstract class OpenWizardFromSelectionAction implements IWorkbenchWindowActionDelegate{

	private ICompilationUnit compilationUnit;

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
	
		try {
			IType type = compilationUnit.getTypes()[0];
			SimpleWizard wizard = createWizard(type);
			
			wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(type));
			
			WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
			dialog.open();
			
		} 
		catch (JavaModelException e) {
			Activator.getDefault().log("error opening wizard", e);			
		}
		
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		compilationUnit = SelectionUtil.getAs(selection, ICompilationUnit.class);
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
	}

	protected abstract SimpleWizard createWizard(IType type);

}
