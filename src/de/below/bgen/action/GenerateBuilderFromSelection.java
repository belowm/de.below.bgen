package de.below.bgen.action;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import de.below.rcp.widgets.SelectionUtil;

public class GenerateBuilderFromSelection extends Action implements IWorkbenchWindowActionDelegate {

	private final GenerateBuilderActionTemplate template = new GenerateBuilderActionTemplate();
	
	private ICompilationUnit compilationUnit;


	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
	
		try {
			template.generate(compilationUnit.getTypes()[0]);
		} 
		catch (JavaModelException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		compilationUnit = SelectionUtil.getAs(selection, ICompilationUnit.class);
		//action.setEnabled(compilationUnit != null);
	}


    @Override
	public void dispose() {

    }

	@Override
	public void init(IWorkbenchWindow window) {
	}

}