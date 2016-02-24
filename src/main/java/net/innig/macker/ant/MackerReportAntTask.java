/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2003 Paul Cantrell
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the license for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *______________________________________________________________________________
 */

package net.innig.macker.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * A task which formats Macker reports using XSLT. Requires Xalan 2 or some
 * other well-behaved XSLT implementation.
 * 
 * @author Paul Cantrell
 * @see <a href="http://ant.apache.org/manual/">The Ant manual</a>
 */
public class MackerReportAntTask extends Task {

	private URL formatUrl;
	private URL skinUrl;
	private URL reportUrl;
	private File outputFile;
	private final TransformerFactory tFactory;

	public MackerReportAntTask() {
		super();
		this.tFactory = TransformerFactory.newInstance();
	}

	public void setFormat(final String formatName) throws BuildException {
		this.formatUrl = resolveInternalResource(formatName, "format", "xsl");
	}

	public void setFormatFile(final File formatFile) throws BuildException {
		this.formatUrl = resolveFile(formatFile, "format");
	}

	public void setFormatUrl(final String formatUrlS) throws BuildException {
		this.formatUrl = resolveUrl(formatUrlS, "format");
	}

	public void setSkin(final String skinName) throws BuildException {
		this.skinUrl = resolveInternalResource(skinName, "skin", "css");
	}

	public void setSkinFile(final File skinFile) throws BuildException {
		this.skinUrl = resolveFile(skinFile, "skin");
	}

	public void setSkinUrl(final String skinUrlS) throws BuildException {
		this.skinUrl = resolveUrl(skinUrlS, "skin");
	}

	public void setXmlReportFile(final File xmlReportFile) throws BuildException {
		this.reportUrl = resolveFile(xmlReportFile, "report");
	}

	public void setXmlReportUrl(final String xmlReportUrlS) throws BuildException {
		this.reportUrl = resolveUrl(xmlReportUrlS, "report");
	}

	public void setOutputFile(final File outputFile) {
		this.outputFile = outputFile;
	}

	private URL resolveFile(final File file, final String kind) throws BuildException {
		if (!file.exists()) {
			throw new BuildException(kind + " file " + file + " does not exist");
		}
		if (!file.isFile()) {
			throw new BuildException(kind + " file " + file + " is not a file");
		}

		try {
			return file.toURI().toURL();
		} catch (MalformedURLException murle) {
			throw new BuildException("Invalid " + kind + " file " + file, murle);
		}
	}

	private URL resolveUrl(final String urlS, final String kind) throws BuildException {
		try {
			return new URL(urlS);
		} catch (MalformedURLException murle) {
			throw new BuildException("Invalid " + kind + " URL " + urlS, murle);
		}
	}

	private URL resolveInternalResource(final String name, final String kind,
			final String extension) throws BuildException {
		final String resourceName = "net/innig/macker/report/" + kind + '/' + name
				+ '.' + extension;
		final URL resource = MackerReportAntTask.class.getResource(resourceName);
		if (resource == null) {
			throw new BuildException("No internal Macker report " + kind
					+ " named \"" + name + "\" (can't find \"" + resourceName
					+ "\")");
		}
		return resource;
	}

	public void execute() throws BuildException {
		if (getReportUrl() == null) {
			throw new BuildException("xmlReportFile or xmlReportUrl required");
		}
		if (getOutputFile() == null) {
			throw new BuildException("outputFile required");
		}

		if (getFormatUrl() == null) {
			setFormat("html-basic");
		}
		if (getSkinUrl() == null) {
			setSkin("vanilla");
		}

		final File outputDir = getOutputFile().getParentFile();

		try {
			final Transformer transformer = getTFactory().newTransformer(new StreamSource(
					getFormatUrl().openStream()));
			transformer.transform(new StreamSource(getReportUrl().openStream()),
					new StreamResult(new FileOutputStream(getOutputFile())));
		} catch (IOException ioe) {
			throw new BuildException("Unable to process report: " + ioe, ioe);
		} catch (TransformerException te) {
			throw new BuildException("Unable to apply report formatting: "
					+ te.getMessage(), te);
		}

		final File skinOutputFile = new File(outputDir, "macker-report.css");
		try {
			IOUtils.copy(getSkinUrl().openStream(), new FileOutputStream(
					skinOutputFile));
			// StreamSplitter is used blocking here, right?
			// new StreamSplitter(skinUrl.openStream(), new
			// FileOutputStream(skinOutputFile)).run();
		} catch (IOException ioe) {
			throw new BuildException(
					"Unable to copy skin to " + skinOutputFile, ioe);
		}
	}
	
	private URL getFormatUrl() {
		return this.formatUrl;
	}
	
	private File getOutputFile() {
		return this.outputFile;
	}
	
	private URL getReportUrl() {
		return this.reportUrl;
	}
	
	private URL getSkinUrl() {
		return this.skinUrl;
	}
	
	public TransformerFactory getTFactory() {
		return this.tFactory;
	}
}
