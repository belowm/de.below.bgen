package de.below.bgen.action;

import org.eclipse.jdt.core.IType;

import de.below.bgen.wizard.NewImplementationClassWizard;
import de.below.rcp.widgets.SimpleWizard;

public class GenerateImplFromEditorAction extends OpenWizardFromEditorAction {

	@Override
	protected SimpleWizard createWizard(IType type) {
		return new NewImplementationClassWizard(type);
	}

}