package de.below.bgen.action;

import org.eclipse.jdt.core.IType;

import de.below.bgen.wizard.NewBuilderWizard;

public class GenerateBuilderFromEditorAction extends OpenWizardFromEditorAction {

	@Override
	protected NewBuilderWizard createWizard(IType type) {
		return new NewBuilderWizard(type);
	}

}