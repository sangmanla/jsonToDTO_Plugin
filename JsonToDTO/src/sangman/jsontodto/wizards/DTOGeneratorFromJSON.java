package sangman.jsontodto.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.json.JSONArray;
import org.json.JSONObject;

import sangman.jsontodto.Util;

public class DTOGeneratorFromJSON extends Wizard implements INewWizard {
	private ConfigurePage configurePage1;
	private ConfigurePage2 configurePage2;
	private ISelection selection;

	/**
	 * Constructor for SampleNewWizard.
	 */
	public DTOGeneratorFromJSON() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		configurePage1 = new ConfigurePage(selection);
		configurePage2 = new ConfigurePage2();
		addPage(configurePage1);
		addPage(configurePage2);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		final String targetPath = configurePage1.getTargetPath();
		final String jsonInfo = configurePage1.getJsonText();
		final boolean isFilePath = configurePage1.isSourcePath();
		final String packageName = configurePage1.getPackageName();
		final String dtoFileName = configurePage2.getRootDtoText();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					JSONObject json = getJsonText(isFilePath, jsonInfo);
					doFinish(targetPath, json, packageName, dtoFileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 */
	private void doFinish(String targetPath, JSONObject json, String packageName, String dtoFileName, IProgressMonitor monitor) throws CoreException {
		boolean hasChild = false;
		StringBuffer body = new StringBuffer();
		
		if (packageName != null && !"".equals(packageName)) {
			body.append("package " + packageName + ";\n");
		}
		
		dtoFileName = Util.getInstance().getCapital(dtoFileName.replaceAll(".java", ""));
		body.append("public class " + dtoFileName.replaceAll(".java", "") + " {\n");

		StringBuffer header = new StringBuffer("");
		StringBuffer methods = new StringBuffer();
		String[] keys = JSONObject.getNames(json);
		for (String key : keys) {
			Object obj = json.opt(key);
			String capKey = Util.getInstance().getCapital(key);
			String type = getSimpleTypeName(obj);
			
			if (obj instanceof JSONArray) {
				JSONArray arr = (JSONArray) obj;
				if(arr.length()>0){
					Object subObj = arr.get(0);
					if(subObj instanceof JSONObject){
						hasChild = true;
						doFinish(targetPath, (JSONObject) subObj, packageName, key, monitor);
						type = Util.getInstance().getCapital(key);
					} else {
						type = getSimpleTypeName(subObj) + "[]";
					}
				}else{
					type = "String";
				}
			}
			header.append("\tprivate " + type + " " + key + ";\n");

			methods.append("\tpublic " + type + " get" + capKey + "(){\n")
					.append("\t\treturn this." + key + ";")
					.append("\n\t}\n")
					.append("\tpublic " + dtoFileName + " set" + capKey + "(" + type + " " + key + "){\n")
					.append("\t\tthis." + key + " = " + key + ";\n")
					.append("\t\treturn this;")
					.append("\n\t}\n");
		}
		body.append("\n")
			.append(header.toString())
			.append(methods.toString())
			.append("}");
		createFile(targetPath, dtoFileName, body.toString(), monitor, hasChild);
	}

	private String getSimpleTypeName(Object obj) {
		String type = obj.getClass().getSimpleName();
		if("Double".equals(type)) type = "double";
		else if("Long".equals(type)) type = "long";
		else if("Integer".equals(type)) type = "int";
		else if("Boolean".equals(type)) type = "boolean";
		else if("Float".equals(type)) type = "float";
		return type;
	}

	private void createFile(String targetPath, String dtoFileName, String content, IProgressMonitor monitor, boolean hasChild) throws CoreException {
		monitor.beginTask("Creating files..", 1);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(targetPath));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + targetPath + "\" does not exist.");
		}
		dtoFileName += (hasChild ? "DTO" : "VO");
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(dtoFileName.endsWith(".java")?dtoFileName:dtoFileName + ".java"));
		try {
			InputStream stream = new ByteArrayInputStream(content.getBytes());
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}

		monitor.worked(1);
	}

	private JSONObject getJsonText(boolean isFilePath, String jsonInfo) throws CoreException {
		JSONObject json = null;
		if (isFilePath) {
			try {
				jsonInfo = new String(Files.readAllBytes(Paths.get(jsonInfo)), StandardCharsets.UTF_8);
			} catch (IOException e) {
				throwCoreException("Invalid json file path or contents in json file('" + jsonInfo + "').");
			}
		}

		try {
			json = new JSONObject(jsonInfo);
		} catch (Exception e) {
			throwCoreException("Wrong json format text. \n " + e.getMessage());
		}
		return json;
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "JsonToDTO", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}