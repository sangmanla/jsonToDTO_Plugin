package sangman.jsontodto.wizards;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class ConfigurePage2 extends WizardPage {
	private Text rootDTOText;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public ConfigurePage2() {
		super("wizardPage");
		setTitle("Generate DTO from JSON - 2");
		setDescription("This wizard creates a new dto java files according to providing json information.");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		setControl(setContainer(parent));
	}
	
	private Composite setContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		layout.verticalSpacing = 9;
		
		new Label(container, SWT.NULL).setText("&Root file name:");
		rootDTOText = new Text(container, SWT.BORDER | SWT.SINGLE);
		rootDTOText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String value = rootDTOText.getText();
				updateStatus(value != null && !"".equals(value));
			}
		});
		
		updateStatus(false);
		
		return container;
	}

	public String getTargetRootFileName() {
		return rootDTOText.getText();
	}
	
	private void updateStatus(boolean thereIsValue) {
		setPageComplete(thereIsValue);
	}
	
	public String getRootDtoText() {
		return rootDTOText.getText();
	}
}