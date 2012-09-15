package de.below.bgen.action;

import org.eclipse.jdt.core.IType;

import de.below.bgen.wizard.NewImplementationClassWizard;

public class GenerateImplFromSelectionAction extends OpenWizardFromSelectionAction {

	@Override
	protected NewImplementationClassWizard createWizard(IType type) {
		NewImplementationClassWizard wizard = new NewImplementationClassWizard(type);
		return wizard;
	}

}