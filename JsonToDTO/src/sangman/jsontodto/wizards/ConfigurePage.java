package sangman.jsontodto.wizards;

import java.io.File;
import java.util.stream.IntStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

public class ConfigurePage extends WizardPage {
	private Text targetPathText;
	private String packageName;

	private Text sourceFilePathText;
	private Text sourceJsonText;
	private Button sourceOption1;
	private Button sourceOption2;

	private Button sourceFileSelectionButton;

	private ISelection selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public ConfigurePage(ISelection selection) {
		super("wizardPage");
		setTitle("Generate DTO from JSON - 1");
		setDescription(
				"This wizard creates a new dto java files according to providing json information.");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_HORIZONTAL);

		Composite container = setContainer(parent);
		Composite container2 = setContainer2(gd, container);

		setTargetPathSection(gd, container2);

		addSpace(container2, 3);

		setSourceJsonSection1(gd, container2);
		setSourceJsonSection2(gd, container, container2);

		initTargetPath();
		dialogChanged();
		setControl(container);
	}

	private void setSourceJsonSection2(GridData gd, Composite container, Composite container2) {
		sourceOption2 = new Button(container2, SWT.RADIO);
		sourceOption2.setText("Input json text.");
		sourceOption2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sourceFilePathText.setEditable(false);
				sourceFileSelectionButton.setEnabled(false);
				sourceJsonText.setEditable(true);
				dialogChanged();
			}
		});
		addSpace(container2, 2);

		sourceJsonText = new Text(container, SWT.BORDER | SWT.MULTI);
		sourceJsonText.setEditable(false);
		sourceJsonText.setLayoutData(gd);
		sourceJsonText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
	}

	private void setSourceJsonSection1(GridData gd, Composite container2) {
		sourceOption1 = new Button(container2, SWT.RADIO);
		sourceOption1.setText("Select json file.");
		sourceOption1.setSelection(true);
		sourceOption1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sourceFilePathText.setEditable(true);
				sourceFileSelectionButton.setEnabled(true);
				sourceJsonText.setEditable(false);
				dialogChanged();
			}
		});
		addSpace(container2, 2);
		
		Label label = new Label(container2, SWT.NULL);
		label.setText("&Json File path:");

		sourceFilePathText = new Text(container2, SWT.BORDER | SWT.SINGLE);
		sourceFilePathText.setLayoutData(gd);
		sourceFilePathText.setEditable(true);
		sourceFilePathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		sourceFileSelectionButton = new Button(container2, SWT.PUSH);
		sourceFileSelectionButton.setText("Browse...");
		sourceFileSelectionButton.setEnabled(true);
		sourceFileSelectionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleSourceBrowse();
			}
		});
	}

	private void addSpace(Composite container2, int cnt) {
		IntStream.range(0, cnt).forEach(x -> { new Label(container2, SWT.NULL); });
	}

	private void setTargetPathSection(GridData gd, Composite container2) {
		Label label = new Label(container2, SWT.NULL);
		label.setText("&Target path :");

		targetPathText = new Text(container2, SWT.BORDER | SWT.SINGLE);
		targetPathText.setLayoutData(gd);
		targetPathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container2, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleTargetBrowse();
			}
		});
	}

	private Composite setContainer2(GridData gd, Composite container) {
		Composite container2 = new Composite(container, SWT.NULL);
		GridLayout layout2 = new GridLayout();
		container2.setLayout(layout2);
		container2.setLayoutData(gd);
		container2.setSize(200, 500);
		layout2.numColumns = 3;
		layout2.verticalSpacing = 9;
		return container2;
	}

	private Composite setContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		layout.verticalSpacing = 9;
		return container;
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initTargetPath() {
		String initContainer = "";
		packageName = "";
		boolean first = true;
		if (selection instanceof TreeSelection) {
			Openable openable = (Openable) ((TreeSelection) selection).getFirstElement();
			while (openable != null) {
				if (!(openable instanceof CompilationUnit)) {
					initContainer = openable.getElementName() + "/" + initContainer;
					if(openable instanceof PackageFragment){
						packageName = openable.getElementName() + "." + packageName;
					}
				}
				openable = (Openable) openable.getParent();
			}
		}
		initContainer = initContainer.replace('.', '/');
		if (initContainer.endsWith("/")) {
			initContainer = initContainer.substring(0, initContainer.length() - 1);
		}
		targetPathText.setText(initContainer);
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleSourceBrowse() {
		FileDialog fDialog = new FileDialog(getShell());
		final String result = fDialog.open();
		if (result != null) {
			final File base = new File(fDialog.getFilterPath());

			for (final String name : fDialog.getFileNames()) {
				this.sourceFilePathText.setText(new File(base, name).getAbsolutePath());
			}
		}
	}
	
	private void handleTargetBrowse() {
		SelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false, "Select target path");
		
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				targetPathText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		String msg = null;
		IResource container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(getTargetPath()));
		if (getTargetPath().length() == 0) {
			msg = "Target path must be provided.";
		}else if (container == null || (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			msg = "Target path must exist.";
		}else if (!container.isAccessible()) {
			msg = "Project must be writable";
		}else if (isSourceInfoEmpty()){
			msg = "Please provide source json information.";
		}
		updateStatus(msg);
	}

	private boolean isSourceInfoEmpty() {
		String txt = null;
		if (sourceOption1 != null && sourceOption1.getSelection() && sourceFilePathText != null) {
			txt = sourceFilePathText.getText();
		}else if(sourceOption2!=null && sourceOption2.getSelection()){
			txt = sourceJsonText.getText();
		}
		return txt == null || "".equals(txt);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getTargetPath() {
		return targetPathText.getText();
	}
	
	public boolean isSourcePath(){
		return sourceOption1.getSelection() ? true : false;
	}

	public String getJsonText() {
		return sourceOption1.getSelection() ? sourceFilePathText.getText() : sourceJsonText.getText();
	}
	public String getPackageName() {
		String packName = packageName;
		if(packName.endsWith(".")){
			packName = packName.substring(0, packName.length() - 1);
		}
		return packName;
	}
}