package de.below.bgen.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import de.below.bgen.Activator;
import de.below.bgen.generator.BuilderGeneratorFacade;
import de.below.bgen.wizard.MethodTableViewer.Columns;
import de.below.rcp.util.ControlUtil;
import de.below.rcp.widgets.SimpleComboViewer;
import de.below.rcp.widgets.SimpleWizard;

@SuppressWarnings("restriction")
public class NewBuilderWizard extends SimpleWizard {

	private static final String DESCRIPTION = "Creates a new Builder for a given class.";

	private static abstract class InitializableWizardPage extends SimpleWizardPage {

		protected InitializableWizardPage(String pageName) {
			super(pageName);
		}

		abstract void initializeDefaults();
		
		
	}

	private SimpleComboViewer<IPackageFragmentRoot> sourceFolderInput;
	private Text sourceTypeInput;
	private Button doEncloseInType;
	private Text builderTypeNameInput;
	private Text builderPackageNameInput;
	private Text enclosingTypeNameInput;
	private SimpleComboViewer<IMethod> constructorInput;
	private MethodTableViewer settersViewer;
	private IType type;
	private Button createFluentSetters;
	private Button prefixedSetters;
	private InitializableWizardPage page1;
	private InitializableWizardPage page2;

	public NewBuilderWizard(IType type) {
		this.type = type;
	}
	
	@Override
	protected void createPages() {

		
		page1 = new InitializableWizardPage("Create Builder") {


			{
				setTitle("New Builder");
				setDescription(DESCRIPTION);
			}

			@Override
			public void validate() throws ValidationError {
				
				if (doEncloseInType.getSelection()) {
					testConstraint(ERROR, getEnclosingType() == null, enclosingTypeNameInput, "Enclosing type not found!");
					testConstraint(ERROR, getEnclosingType().isReadOnly(), enclosingTypeNameInput, "Enclosing type not writeable!");
				}
			
			}

			@Override
			public void createControl(Composite parent) {
				
				initializeDialogUnits(parent);
				
				Composite composite = new Composite(parent, SWT.NONE);
				composite.setFont(parent.getFont());

				GridLayoutFactory.fillDefaults().numColumns(4).spacing(8, 6).applyTo(composite);
				
				createSourceTypeSection(composite);
				
				createSeparator(composite);
				
				createBuilderTypeNameSection(composite);
				createEnclosingTypeSection(composite);
				
				createSourceFolderSection(composite);
				createPackageSection(composite);

				createSeparator(composite);
				
				createOptionsSection(composite);
				
				initializeDefaults();
				
				setControl(composite);

				attachValidationListenerTo(enclosingTypeNameInput, doEncloseInType);
				
				doValidation();
				
			}

			private void createOptionsSection(Composite composite) {

				GridDataFactory gridDataFactory = GridDataFactory.swtDefaults().span(2, 1);
				
				newLabel(composite, "Naming convention for Setter-Methods", 4);
				new Label(composite, SWT.NULL);
				
				Button noSetterPrefix = newButton(composite, "foo(...)", SWT.RADIO);
				noSetterPrefix.setSelection(true);
				gridDataFactory.applyTo(noSetterPrefix);
				
				new Label(composite, SWT.NULL);
				new Label(composite, SWT.NULL);
				
				prefixedSetters = newButton(composite, "setFoo(...)", SWT.RADIO);
				gridDataFactory.applyTo(prefixedSetters);
				
				newLabel(composite, "Do you want 'Fluent Setters' where all " +
						"setters return the builder itself?", 4);
				new Label(composite, SWT.NULL);
				createFluentSetters = newButton(composite, "Generate Fluent API", SWT.CHECK);
				createFluentSetters.setSelection(true);
				
			}

			@Override
			void initializeDefaults() {
				if (type != null) {
					sourceTypeInput.setText(type.getFullyQualifiedName());
					builderTypeNameInput.setText(type.getElementName() + "Builder");
					builderPackageNameInput.setText(type.getPackageFragment().getElementName());
					doEncloseInType.setSelection(true);
					enclosingTypeNameInput.setText(type.getFullyQualifiedName());
					
					ControlUtil.raiseEvent(doEncloseInType, SWT.Selection, new Event());
				}
			}

			private void createSeparator(Composite composite) {
				new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)
					.setLayoutData(GridDataFactory.fillDefaults().span(4, 1).hint(SWT.DEFAULT, 8).create());
			}

			private void createBuilderTypeNameSection(Composite composite) {
				newLabel(composite, "Builder Class Name:", 1);
				builderTypeNameInput = newText(composite, 2);
				new Label(composite, SWT.NULL);
			}

			private void createSourceTypeSection(Composite composite) {
				newLabel(composite, "Class to create Builder for:", 1);
				sourceTypeInput = newText(composite, 2);
				Button button = newButton(composite, "Browse...", SWT.PUSH);
				
				button.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event event) {
						
						IType newType = openSelectSourceTypeDialog();
						
						if (newType != null) {
							type = newType;
							page1.initializeDefaults();
							page2.initializeDefaults();
						}
					}

				});
				
				
			}

			private void createSourceFolderSection(Composite composite) {
				newLabel(composite, "Source folder:", 1);

				sourceFolderInput = new SimpleComboViewer<IPackageFragmentRoot>(
						composite, SWT.BORDER | SWT.READ_ONLY, IPackageFragmentRoot.class,
						LabelProviders.SOURCE_FOLDER);
				
				
				GridDataFactory.fillDefaults().span(2, 1).applyTo(sourceFolderInput.getControl());
				
				IJavaProject project = (IJavaProject) type.getAncestor(IJavaElement.JAVA_PROJECT);
				
				try {
					List<IPackageFragmentRoot> sourceFolder = getSourceFolderOf(project);
					sourceFolderInput.setInput(sourceFolder);
					sourceFolderInput.setSelection(sourceFolder.get(0));
				} 
				catch (JavaModelException e) {
					Activator.getDefault().log("error loading source packages for project " + project, e);
				}
				
				
				new Label(composite, SWT.NULL);
				
				ControlUtil.updateEnableStateOnCheckbox(doEncloseInType, true,
						sourceFolderInput.getControl());
				
				
			}

			private List<IPackageFragmentRoot> getSourceFolderOf(IJavaProject project)
					throws JavaModelException {

				IPackageFragmentRoot[] packageFragmentRoots = project.getPackageFragmentRoots();
				
				List<IPackageFragmentRoot> result = new ArrayList<IPackageFragmentRoot>();
				for (IPackageFragmentRoot fragmentRoot : packageFragmentRoots) {
					
					if (fragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
						result.add(fragmentRoot);
					}
				}
				
				return result;
			}
			
			
			private void createPackageSection(Composite composite) {
				newLabel(composite, "Package:", 1);
				builderPackageNameInput = newText(composite, 2);
				Button browseButton = newButton(composite, "Browse...", SWT.PUSH);
				
				ControlUtil.updateEnableStateOnCheckbox(doEncloseInType, true,
						builderPackageNameInput, browseButton);
				
				browseButton.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event event) {
						
						IPackageFragment selectedPackage = openSelectPackageDialog();
						
						if (selectedPackage != null) {
							builderPackageNameInput.setText(selectedPackage.getElementName());
						}
					}
				});
				
			}

			private void createEnclosingTypeSection(Composite composite) {
				doEncloseInType = newButton(composite, "Enclosing Type:", SWT.CHECK);
				enclosingTypeNameInput = newText(composite, 2);
				final Button browseEnclosingTypes = newButton(composite, "Browse...", SWT.PUSH);
				
				ControlUtil.updateEnableStateOnCheckbox(doEncloseInType, false,
						enclosingTypeNameInput, browseEnclosingTypes);
				
			}

		};
		
		page2 = new InitializableWizardPage("Select properties") {
			
			{
				setDescription(DESCRIPTION);
			}
			
			@Override
			public void validate() throws ValidationError {
				
			}
			
			@Override
			public void createControl(Composite parent) {
				
				initializeDialogUnits(parent);
				
				Composite composite = new Composite(parent, SWT.NONE);
				composite.setFont(parent.getFont());
				
				GridLayoutFactory.fillDefaults().numColumns(2).spacing(8, 6).applyTo(composite);
				
				createConstructorSection(composite);
				createPropertyViewer(composite);
				
				initializeDefaults();
				
				setControl(composite);
			}

			private void createPropertyViewer(Composite composite) {
				
				newLabel(composite, "Include additional properties from setters:", 2);
				
				settersViewer = new MethodTableViewer(composite, SWT.BORDER,
						SWT.FULL_SELECTION | SWT.CHECK, Columns.SIGNATURE);
				
				GridDataFactory.fillDefaults().grab(true, false).span(2, 1)
						.hint(SWT.DEFAULT, 240)
						.applyTo(settersViewer);
				
				constructorInput.getControl().addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event event) {
						loadSetters();
					}
					
				});
				
			}
			
			@Override
			protected void initializeDefaults() {
				

				List<IMethod> constructors = findConstructorsFor(type);
				constructorInput.setInput(constructors);
				
				if (!constructors.isEmpty()) {
					constructorInput.setSelection(constructors.get(0));
				}
				
				loadSetters();

			}

			private void loadSetters() {
				Collection<IMethod> setters = getSettersFor(type, constructorInput.getSelection());
				settersViewer.setInput(setters);
				settersViewer.setChecked(setters, true);
			}

			private void createConstructorSection(Composite composite) {

				newLabel(composite, "Constructor to call: ", 1);
				
				constructorInput = new SimpleComboViewer<IMethod>(
						composite, SWT.READ_ONLY, IMethod.class, LabelProviders.METHOD);
				
				GridDataFactory.fillDefaults().grab(true, false).applyTo(constructorInput.getControl());
				
			}

		};
		
		addPage(page1);
		addPage(page2);
		
	}

	private IPackageFragment openSelectPackageDialog() {
			
		IPackageFragmentRoot froot = getSourceFolder();
			
		IJavaElement[] packages= null;
		try {
			if (froot != null && froot.exists()) {
				packages= froot.getChildren();
			}
		} catch (JavaModelException e) {
			Activator.getDefault().log("error loading packages from sourcefolder", e);
		}
		if (packages == null) {
			packages= new IJavaElement[0];
		}

		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), new JavaElementLabelProvider(
						JavaElementLabelProvider.SHOW_DEFAULT));
		
		dialog.setIgnoreCase(false);
		dialog.setTitle("Choose package");
		dialog.setMessage("Select the package where the builder should be placed in");
		dialog.setEmptyListMessage("Select the package where the builder should be placed in");
		dialog.setElements(packages);
		dialog.setHelpAvailable(false);

		IPackageFragment pack= (IPackageFragment) type.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		if (pack != null) {
			dialog.setInitialSelections(new Object[] { pack });
		}

		if (dialog.open() == Window.OK) {
			return (IPackageFragment) dialog.getFirstResult();
		}
		
		return null;
		
	}

	
	private IType openSelectSourceTypeDialog() {

		IPackageFragmentRoot root = getSourceFolder();
		if (root == null) {
			return null;
		}

		IJavaSearchScope scope = SearchEngine
				.createJavaSearchScope(new IJavaElement[] { type
						.getAncestor(IJavaElement.JAVA_PROJECT) });

		FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(getShell(),
			false, getContainer(), scope, IJavaSearchConstants.TYPE);
		dialog.setTitle("Chose enclosing type");
		dialog.setMessage("Please chose the enclosing type where the builder should be created in.");
		dialog.setInitialPattern(Signature.getSimpleName(type.getElementName()));

		if (dialog.open() == Window.OK) {
			return (IType) dialog.getFirstResult();
		}
		return null;
		
	}

	
	private IPackageFragmentRoot getSourceFolder() {
		return sourceFolderInput.getSelection();
	}

	private Collection<IMethod> getSettersFor(IType type, IMethod constructor) {
		
		List<IMethod> result = new ArrayList<IMethod>();
		
		List<String> parametersFromConstructor = new ArrayList<String>();
		
		try {
			if (constructor != null) {
				
				for (String paramName : constructor.getParameterNames()) {
					parametersFromConstructor.add(paramName.toLowerCase());
				}
			}
		
			for (IMethod method : type.getMethods()) {
				
				String methodName = method.getElementName();
				
				if (isVisible(method) && methodName.startsWith("set") && methodName.length() > 3) {
					
					if (method.getParameterNames().length == 1) {
						
						if (!parametersFromConstructor.contains(methodName.substring(3).toLowerCase())) {
							result.add(method);
						}
						
					}
					
				}
				
			}
		}
		catch (JavaModelException e) {
			// syntax error on method -> swallow
		}
		
		
		return result;
	}

	private List<IMethod> findConstructorsFor(IType type) {
		
		List<IMethod> result = new ArrayList<IMethod>();

		try {
			for (IMethod method : type.getMethods()) {
				if (isVisible(method) && (method.isConstructor() || isFactoryMethod(method)) ) {
					result.add(method);
				}
			}
		}
		catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	private boolean isFactoryMethod(IMethod method) throws JavaModelException {
		
		return (method.getFlags() & Flags.AccStatic) > 0 && 
			Signature.toString(method.getReturnType()).equals(type.getFullyQualifiedName());
	}

	private boolean isVisible(IMethod method) throws JavaModelException {
		return (method.getFlags() & Flags.AccPublic) > 0;
	}

	
	private Button newButton(Composite parent, String label, int style) {
		
		Button button = new Button(parent, style);
		button.setFont(parent.getFont());
		button.setText(label);
		return button;
	}
	
	private Text newText(Composite parent, int span) {
		GridData gd = new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.horizontalSpan= span;
		
		Text text = new Text(parent, SWT.BORDER);
		text.setLayoutData(gd);
		return text;
	}
	
	private Label newLabel(Composite composite, String text, int colSpan) {
		
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = colSpan;
		Label label = new Label(composite, SWT.NULL);
		label.setText(text);
		label.setLayoutData(gd);
		return label;
	}

	@Override
	public boolean performFinish() {
		
		try {
			BuilderGeneratorFacade generator = BuilderGeneratorFacade
				.newInstance()
				.withType(type)
				.withSetterProperties(settersViewer.getChecked())
				.withFluentSetters(createFluentSetters.getSelection())
				.withBuilderName(builderTypeNameInput.getText())
				.withSetterPrefix(prefixedSetters.getSelection() ? "set" : null)
				;
			
			IMethod constructorMethod = constructorInput.getSelection();
			if (constructorMethod != null) {
				if (constructorMethod.isConstructor()) {
					generator.withConstructor(constructorMethod);
				}
				else {
					generator.withFactoryMethod(constructorMethod);
				}
			}
			
			if (doEncloseInType.getSelection()) {
				generator.withEnclosingType(type);
			}
			else {
				generator.withTargetPackageName(getSourceFolder(), builderPackageNameInput.getText());
			}

			generator.generate();
		} 
		catch (JavaModelException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", e.toString());
		}
		
		return true;
		
	}

	private IType getEnclosingType() {
		
		IJavaProject project = (IJavaProject) type.getAncestor(IJavaElement.JAVA_PROJECT);
		try {
			return project.findType(enclosingTypeNameInput.getText());
		} 
		catch (JavaModelException e) {
			return null;
		}
		
	}
		
}
