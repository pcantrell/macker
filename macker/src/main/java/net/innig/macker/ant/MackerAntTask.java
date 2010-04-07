/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002-2003 Paul Cantrell
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

import net.innig.macker.Macker;
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.rule.RuleSeverity;
import net.innig.macker.rule.RulesException;
import net.innig.macker.structure.ClassParseException;
import net.innig.macker.structure.IncompleteClassInfoException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

/**
 * A task which allows access to Macker from Ant build files.
 * 
 * @see <a href="http://ant.apache.org/manual/">The Ant manual</a>
 * 
 * @author Paul Cantrell
 */
public class MackerAntTask extends Task {
	
	private static final String MACKER_CHOKED_MESSAGE = "Macker configuration failed";
	private static final String MACKER_IS_MAD_MESSAGE = "Macker rules checking failed";
	
	private boolean fork = false;
	private boolean failOnError = true;
	private boolean verbose = false;
	private List<String> jvmArgs;
	
	private Path classpath;
	private String angerProperty;
	
	// for non-forked
	private Macker macker;
	private Java jvm;
	
	/**
	 * Create a new {@link MackerAntTask} instance.
	 *
	 */
	public MackerAntTask() {
		super();
		setMacker(new Macker());
		setJvmArgs(new ArrayList<String>());
	}

	public void execute() throws BuildException {
		if (isVerbose()) {
			System.out.println("Macker (verbose mode enabled)");
		}
		if (isFailOnError() && getAngerProperty() != null) {
			System.out.println("WARNING: failOnError is set, so angerProperty will have no effect");
		}
		try {
			if (!isFork()) {
				if (getClasspath() != null) {
					getMacker().setClassLoader(new AntClassLoader(getProject(), getClasspath(), false));
				}

				getMacker().check();
			} else {
				if (getClasspath() == null) {
					throw new BuildException("nested <classpath> element is required when fork=true");
				}

				getJvm().setTaskName("macker");
				getJvm().setClassname("net.innig.macker.Macker");
				getJvm().setFork(isFork());
				getJvm().setFailonerror(false);
				getJvm().clearArgs();

				for (String arg : getJvmArgs()) {
					getJvm().createArg().setValue(arg);
				}

				final int resultCode = getJvm().executeJava();
				if (resultCode == 2) {
					throw new MackerIsMadException();
				}
				if (resultCode != 0) {
					throw new BuildException(MACKER_CHOKED_MESSAGE);
				}
			}
		} catch (MackerIsMadException mime) {
			if (mime.getMessage() != null) {
				printMessageChain(mime);
			}
			if (getAngerProperty() != null) {
				getProject().setProperty(getAngerProperty(), "true");
			}
			if (isFailOnError()) {
				throw new BuildException(MACKER_IS_MAD_MESSAGE);
			}
		} catch (ListenerException lie) {
			printMessageChain(lie);
			throw new BuildException(MACKER_CHOKED_MESSAGE);
		} catch (RulesException rue) {
			printMessageChain(rue);
			throw new BuildException(MACKER_CHOKED_MESSAGE);
		} catch (IncompleteClassInfoException icie) {
			printMessageChain(icie);
			throw new BuildException(MACKER_CHOKED_MESSAGE);
		}
	}

	public void setFork(final boolean fork) {
		this.fork = fork;
	}

	public void setFailOnError(final boolean failOnError) {
		this.failOnError = failOnError;
	}

	public void setMaxMessages(final int printMaxMessages) {
		getMacker().setPrintMaxMessages(printMaxMessages);
		getJvmArgs().add("--print-max");
		getJvmArgs().add(String.valueOf(printMaxMessages));
	}

	public void setPrintThreshold(final String threshold) {
		getMacker().setPrintThreshold(RuleSeverity.fromName(threshold));
		getJvmArgs().add("--print");
		getJvmArgs().add(threshold);
	}

	public void setAngerThreshold(final String threshold) {
		getMacker().setAngerThreshold(RuleSeverity.fromName(threshold));
		getJvmArgs().add("--anger");
		getJvmArgs().add(threshold);
	}

	public void setAngerProperty(final String property) {
		this.angerProperty = property;
	}

	public void setVerbose(final boolean verbose) {
		this.verbose = verbose;
		getMacker().setVerbose(verbose);
		if (verbose) {
			getJvmArgs().add("-v");
		}
	}

	public Path createClasspath() {
		setClasspath(getJvm().createClasspath());
		return getClasspath();
	}

	public void addConfiguredVar(final Var var) {
		getMacker().setVariable(var.getName(), var.getValue());
		getJvmArgs().add("-D");
		getJvmArgs().add(var.getName() + "=" + var.getValue());
	}

	public void setXmlReportFile(final File xmlReportFile) {
		getMacker().setXmlReportFile(xmlReportFile);
		getJvmArgs().add("-o");
		getJvmArgs().add(xmlReportFile.getPath());
	}

	public static class Var {
		
		private String name;
		private String value;
		
		public String getName() {
			return this.name;
		}

		public String getValue() {
			return this.value;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public void setValue(final String value) {
			this.value = value;
		}
	}

	public void addConfiguredClasses(final FileSet classFiles) throws IOException {
		final DirectoryScanner classScanner = classFiles.getDirectoryScanner(getProject());
		final String[] fileNames = classScanner.getIncludedFiles();
		final File baseDir = classScanner.getBasedir();
		for (int n = 0; n < fileNames.length; n++) {
			final File classFile = new File(baseDir, fileNames[n]);
			if (!classFile.getName().endsWith(".class")) {
				System.out.println("WARNING: " + fileNames[n] + " is not a .class file; ignoring");
			}
			getJvmArgs().add(classFile.getPath());
			try {
				getMacker().addClass(classFile);
			} catch (ClassParseException cpe) {
				printMessageChain("Unable to parse class file: " + classFile.getPath(), cpe);
				throw new BuildException(MACKER_CHOKED_MESSAGE);
			}
		}
	}

	public void addConfiguredRules(final FileSet rulesFiles) throws IOException {
		final DirectoryScanner rulesScanner = rulesFiles.getDirectoryScanner(getProject());
		final String[] fileNames = rulesScanner.getIncludedFiles();
		final File baseDir = rulesScanner.getBasedir();
		for (int n = 0; n < fileNames.length; n++) {
			final File rulesFile = new File(baseDir, fileNames[n]);
			getJvmArgs().add("-r");
			getJvmArgs().add(rulesFile.getPath());
			try {
				getMacker().addRulesFile(rulesFile);
			} catch (RulesException re) {
				printMessageChain(re);
				throw new BuildException(MACKER_CHOKED_MESSAGE);
			}
		}
	}

	private Java getJvm() {
		if (this.jvm == null) {
			this.jvm = new Java();
			this.jvm.setProject(getProject());
		}
		return this.jvm;
	}

	private void printMessageChain(final Throwable e) {
		printMessageChain("", e);
	}

	private void printMessageChain(final String message, Throwable e) {
		System.out.println(message);
		for (; e != null; e = e.getCause()) {
			System.out.println(e.getMessage());
		}
	}
	
	private String getAngerProperty() {
		return this.angerProperty;
	}
	
	private Path getClasspath() {
		return this.classpath;
	}
	
	private void setClasspath(final Path classpath) {
		this.classpath = classpath;
	}
	
	private boolean isFailOnError() {
		return this.failOnError;
	}
	
	private boolean isFork() {
		return this.fork;
	}
	
	private List<String> getJvmArgs() {
		return this.jvmArgs;
	}
	
	private void setJvmArgs(final List<String> jvmArgs) {
		this.jvmArgs = jvmArgs;
	}
	
	private Macker getMacker() {
		return this.macker;
	}
	
	private void setMacker(final Macker macker) {
		this.macker = macker;
	}
	
	private boolean isVerbose() {
		return this.verbose;
	}
}
