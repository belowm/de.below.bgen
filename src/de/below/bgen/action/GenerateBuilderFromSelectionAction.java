package de.below.bgen.action;

import org.eclipse.jdt.core.IType;

import de.below.bgen.wizard.NewBuilderWizard;

public class GenerateBuilderFromSelectionAction extends OpenWizardFromSelectionAction {

	@Override
	protected NewBuilderWizard createWizard(IType type) {
		NewBuilderWizard wizard = new NewBuilderWizard(type);
		return wizard;
	}

}