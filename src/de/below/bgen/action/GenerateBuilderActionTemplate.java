package de.below.bgen.action;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import de.below.bgen.wizard.NewBuilderWizard;

public class GenerateBuilderActionTemplate {

	public void generate(IType type) {
		
		try {
			
			NewBuilderWizard wizard = new NewBuilderWizard(type);
			
			wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(type));
			
			WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
			dialog.open();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
