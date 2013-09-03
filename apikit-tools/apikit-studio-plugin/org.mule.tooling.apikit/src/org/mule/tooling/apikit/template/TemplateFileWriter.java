package org.mule.tooling.apikit.template;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

public class TemplateFileWriter {

	private IProject project;
	private IProgressMonitor monitor;
    
	
	public TemplateFileWriter(IProject project, IProgressMonitor monitor) {
		this.project = project;
		this.monitor = monitor;
	}


	public void apply(final String templatePath, final String resultPath, 
			Replacer replacer)
			throws CoreException {
		final IFile pomFile = project.getProject().getFile(resultPath);

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		Writer writer = null;
		Reader reader = null;

		try {
			monitor.beginTask("Creating file " + resultPath, 100);

			InputStream pomTemplateResource;

			pomTemplateResource = TemplateFileWriter.class.getClassLoader()
					.getResourceAsStream(templatePath);

			reader = new InputStreamReader(pomTemplateResource, "UTF-8");
			writer = new OutputStreamWriter(byteArrayOutputStream, "UTF-8");

			replacer.replace(reader, writer);

			writer.flush();

			monitor.worked(30);

			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					byteArrayOutputStream.toByteArray());

			pomFile.create(byteArrayInputStream, false, new SubProgressMonitor(monitor, 40));
			pomFile.setDerived(false, new SubProgressMonitor(monitor, 30));

		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
			monitor.done();
			if (writer != null) {
				IOUtils.closeQuietly(writer);
			}
			if (reader != null) {
				IOUtils.closeQuietly(reader);
			}
		}

	}

}
