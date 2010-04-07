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

package net.innig.macker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.innig.collect.GraphWalker;
import net.innig.collect.Graphs;
import net.innig.collect.InnigCollections;
import net.innig.collect.Selector;
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerEventListener;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.event.PrintingListener;
import net.innig.macker.event.ThrowingListener;
import net.innig.macker.event.XmlReportingListener;
import net.innig.macker.rule.EvaluationContext;
import net.innig.macker.rule.Pattern;
import net.innig.macker.rule.RuleSet;
import net.innig.macker.rule.RuleSetBuilder;
import net.innig.macker.rule.RuleSeverity;
import net.innig.macker.rule.RulesException;
import net.innig.macker.structure.ClassInfo;
import net.innig.macker.structure.ClassManager;
import net.innig.macker.structure.ClassParseException;
import net.innig.macker.structure.IncompleteClassInfoException;

import org.apache.commons.io.IOUtils;

/**
 * The command line interface for Macker.
 * 
 * @author Paul Cantrell
 */
public class Macker {
	/** The class manager. */
	private ClassManager classManager;
	/** The rule sets. */
	private Collection<RuleSet> ruleSets;
	/** The command line passed variables. */
	private Map<String, String> vars;
	/** Flag indicating if output should be verbose. */
	private boolean verbose;
	/** The XML file to report to. */
	private File xmlReportFile;
	/** The registered event listeners. */
	private List<MackerEventListener> listeners = new ArrayList<MackerEventListener>();
	/** The maximum number of messages to print. */
	private int printMaxMessages;
	/** The lowest rule severity level to print. */
	private RuleSeverity printThreshold = RuleSeverity.INFO;
	/** The lowest rule severity level to consider as an error. */
	private RuleSeverity angerThreshold = RuleSeverity.ERROR;

	/**
	 * Create a new {@link Macker} instance.
	 */
	public Macker() {
		this.classManager = new ClassManager();
		this.ruleSets = new ArrayList<RuleSet>();
		this.vars = new HashMap<String, String>();
		this.verbose = false;
	}

	/**
	 * Add a primary class to check to the class manager.
	 * 
	 * @param classFile The class to check.
	 * 
	 * @throws IOException When reading the class failed.
	 * @throws ClassParseException When the class couldn't be parsed.
	 */
	public void addClass(final File classFile) throws IOException, ClassParseException {
		makePrimary(getClassManager().readClass(classFile));
	}

	/**
	 * Add a primary class to check to the class manager.
	 * 
	 * @param classFile The class to check.
	 * 
	 * @throws IOException When reading the class failed.
	 * @throws ClassParseException When the class couldn't be parsed.
	 */
	public void addClass(final InputStream classFile) throws IOException, ClassParseException {
		makePrimary(getClassManager().readClass(classFile));
	}

	/**
	 * Add a primary class to check to the class manager.
	 * 
	 * @param classFile The name of the class to check.
	 * 
	 * @throws IOException When reading the class failed.
	 * @throws ClassParseException When the class couldn't be parsed.
	 */
	public void addClass(final String className) throws ClassNotFoundException {
		makePrimary(getClassManager().getClassInfo(className));
	}
	
	/**
	 * Make a ClassInfo object the primary.
	 * 
	 * @param classInfo the ClassInfo object
	 */
	private void makePrimary(final ClassInfo classInfo) {
		getClassManager().makePrimary(classInfo);
	}

	/**
	 * Add all the classes from the provided file.
	 * <p>
	 * The text file should contain a single class name per line.
	 * </p>
	 * 
	 * @param fileName The name of the file containing the classes.
	 * 
	 * @throws IOException When reading of the text file or class files failed.
	 * @throws ClassParseException When the classes couldn't be parsed.
	 */
	public void addClassesFromFile(final String fileName) throws IOException, ClassParseException {
		final File indexFile = new File(fileName);
		BufferedReader indexReader = null;
		try {
			indexReader = new BufferedReader(new FileReader(indexFile));
			for (String line; (line = indexReader.readLine()) != null;) {
				addClass(new File(line));
			}
		} finally {
			IOUtils.closeQuietly(indexReader);
		}
	}

	/**
	 * For determining the primary classes when you don't have a hard-coded class list, or knowledge of the file system
	 * where classes are stored. Determines the set of primary classes by walking the class reference graph out from the
	 * initial class name, and marking all classes which start with primaryPrefix.
	 * 
	 * @param initialClassName The initial class.
	 * @param primaryPrefix The primary prefix to check on for determining a primary class.
	 * 
	 * @throws IncompleteClassInfoException When the class info is incomplete or could not be constructed.
	 */
	public void addReachableClasses(final Class<?> initialClass, final String primaryPrefix)
			throws IncompleteClassInfoException {
		addReachableClasses(initialClass.getName(), primaryPrefix);
	}

	/**
	 * For determining the primary classes when you don't have a hard-coded class list, or knowledge of the file system
	 * where classes are stored. Determines the set of primary classes by walking the class reference graph out from the
	 * initial class name, and marking all classes which start with primaryPrefix.
	 * 
	 * @param initialClassName The name of the initial class.
	 * @param primaryPrefix The primary prefix to check on for determining a primary class.
	 * 
	 * @throws IncompleteClassInfoException When the class info is incomplete or could not be constructed.
	 */
	public void addReachableClasses(final String initialClassName, final String primaryPrefix)
			throws IncompleteClassInfoException {
		Graphs.reachableNodes(getClassManager().getClassInfo(initialClassName), new GraphWalker<ClassInfo>() {
			public Collection<ClassInfo> getEdgesFrom(final ClassInfo classInfo) {
				makePrimary(classInfo);
				return InnigCollections.select(classInfo.getReferences().keySet(), new Selector<ClassInfo>() {
					public boolean select(final ClassInfo classInfo) {
						return classInfo.getFullClassName().startsWith(primaryPrefix);
					}
				});
			}
		});
	}

	/**
	 * Check if there are primary classes are loaded for validation.
	 * 
	 * @return <code>true</code> if there are primary classes loaded.
	 */
	public boolean hasClasses() {
		return !getClassManager().getPrimaryClasses().isEmpty();
	}

	/**
	 * Add a file containing the rules to use.
	 * 
	 * @param rulesFile The rules file.
	 * 
	 * @throws IOException When reading the file failed.
	 * @throws RulesException When the rules could not be build.
	 */
	public void addRulesFile(final File rulesFile) throws IOException, RulesException {
		getRuleSets().addAll(new RuleSetBuilder().build(rulesFile));
	}

	/**
	 * Add a file containing the rules to use.
	 * 
	 * @param rulesFile {@link InputStream} to the rules file.
	 * 
	 * @throws IOException When reading the file failed.
	 * @throws RulesException When the rules could not be build.
	 */
	public void addRulesFile(final InputStream rulesFile) throws IOException, RulesException {
		getRuleSets().addAll(new RuleSetBuilder().build(rulesFile));
	}

	/**
	 * Add a {@link RuleSet} for execution on the classes.
	 * 
	 * @param ruleSet The {@link RuleSet} to load.
	 * 
	 * @throws IOException When the ruleset could not be loaded.
	 * @throws RulesException When the ruleset was invalid.
	 */
	public void addRuleSet(final RuleSet ruleSet) throws IOException, RulesException {
		getRuleSets().add(ruleSet);
	}

	/**
	 * Add an {@link MackerEventListener} for handling {@link MackerEvent} events.
	 * 
	 * @param listener The {@link MackerEventListener}.
	 */
	public void addListener(final MackerEventListener listener) {
		getListeners().add(listener);
	}

	/**
	 * Check if there is any {@link RuleSet} loaded.
	 * 
	 * @return <code>true</code> if there is atleast 1 {@link RuleSet} loaded.
	 */
	public boolean hasRules() {
		return !getRuleSets().isEmpty();
	}

	/**
	 * Set a Macker variable.
	 * 
	 * @param name The name of the variable.
	 * @param value The value of the variable.
	 */
	public void setVariable(final String name, final String value) {
		getVars().put(name, value);
	}

	/**
	 * Indicate if Macker should be verbose in it's output.
	 * 
	 * @param verbose <code>true</code> for verbose output.
	 */
	public void setVerbose(final boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Set the ClassLoader to use.
	 * 
	 * @param classLoader The ClassLoader.
	 */
	public void setClassLoader(final ClassLoader classLoader) {
		getClassManager().setClassLoader(classLoader);
	}

	/**
	 * Set the maximum number of messages to show.
	 * 
	 * @param printMaxMessages The maximum number of messages.
	 */
	public void setPrintMaxMessages(final int printMaxMessages) {
		this.printMaxMessages = printMaxMessages;
	}

	/**
	 * Set the lowest {@link RuleSeverity} level to show output from.
	 * 
	 * @param printThreshold The lowest {@link RuleSeverity} level to show output from.
	 */
	public void setPrintThreshold(final RuleSeverity printThreshold) {
		this.printThreshold = printThreshold;
	}

	/**
	 * Set the lowest {@link RuleSeverity} level to cause Macker to report an error.
	 * 
	 * @param angerThreshold The lowest {@link RuleSeverity} level to report an error from.
	 */
	public void setAngerThreshold(final RuleSeverity angerThreshold) {
		this.angerThreshold = angerThreshold;
	}

	/**
	 * Set the XML report file to use.
	 * 
	 * @param xmlReportFile The xml report file to use.
	 */
	public void setXmlReportFile(final File xmlReportFile) {
		this.xmlReportFile = xmlReportFile;
	}

	/**
	 * Performs rule checking with the default printing, throwing, and XML reporting listeners.
	 * 
	 * @throws MackerIsMadException TODO Document me!
	 * @throws RulesException TODO Document me!
	 * @throws ListenerException TODO Document me!
	 **/
	public void check() throws MackerIsMadException, RulesException, ListenerException {
		if (!hasRules()) {
			System.out.println("WARNING: No rules files specified");
		}
		if (!hasClasses()) {
			System.out.println("WARNING: No class files specified");
		}

		if (isVerbose()) {
			System.out.println(getClassManager().getPrimaryClasses().size() + " primary classes");
			System.out.println(getClassManager().getAllClasses().size() + " total classes");
			System.out.println(getClassManager().getReferences().size() + " references");

			for (ClassInfo classInfo : getClassManager().getPrimaryClasses()) {
				System.out.println("Classes used by " + classInfo + ":");
				for (ClassInfo used : classInfo.getReferences().keySet()) {
					System.out.println("    " + used);
				}
				System.out.println();
			}
		}

		PrintingListener printing;
		if (getPrintThreshold() == null) {
			printing = null;
		} else {
			printing = new PrintingListener(System.out);
			printing.setThreshold(getPrintThreshold());
			if (getPrintMaxMessages() > 0) {
				printing.setMaxMessages(getPrintMaxMessages());
			}
			addListener(printing);
		}

		ThrowingListener throwing;
		if (getAngerThreshold() == null) {
			throwing = null;
		} else {
			throwing = new ThrowingListener(null, getAngerThreshold());
			addListener(throwing);
		}

		XmlReportingListener xmlReporting = null;
		if (getXmlReportFile() != null) {
			xmlReporting = new XmlReportingListener(getXmlReportFile());
			addListener(xmlReporting);
		}

		checkRaw();

		if (printing != null) {
			printing.printSummary();
		}
		if (xmlReporting != null) {
			xmlReporting.flush();
			xmlReporting.close();
		}
		if (throwing != null) {
			throwing.timeToGetMad();
		}
	}

	/**
	 * Performs rule checking without any default listeners.
	 * 
	 * @throws MackerIsMadException TODO Document me!
	 * @throws RulesException TODO Document me!
	 * @throws ListenerException TODO Document me!
	 */
	public void checkRaw() throws MackerIsMadException, RulesException, ListenerException {
		for (RuleSet rs : getRuleSets()) {
			if (isVerbose()) {
				for (final Pattern pat : rs.getAllPatterns()) {
					final EvaluationContext ctx = new EvaluationContext(getClassManager(), rs);
					System.out.println("matching " + pat);
					for (ClassInfo classInfo : getClassManager().getPrimaryClasses()) {
						if (pat.matches(ctx, classInfo)) {
							System.out.println("    " + classInfo);
						}
					}
					System.out.println();
				}
			}

			final EvaluationContext context = new EvaluationContext(getClassManager(), rs);
			context.setVariables(getVars());
			for (MackerEventListener listener : getListeners()) {
				context.addListener(listener);
			}

			rs.check(context, getClassManager());
		}
	}

	/**
	 * Main class entry point for command line execution.
	 * 
	 * @param args The command line arguments.
	 * 
	 * @throws Exception When execution fails.
	 */
	public static void main(final String[] args) throws Exception {
		try {
			// Parse args
			final Macker macker = new Macker();

			boolean nextIsRule = false;
			for (int arg = 0; arg < args.length; arg++) {
				if ("-h".equals(args[arg]) || "-help".equals(args[arg]) || "--help".equals(args[arg])) {
					commandLineUsage();
					return;
				} else if ("-V".equals(args[arg]) || "--version".equals(args[arg])) {
					final Properties p = new Properties();
					p.load(Macker.class.getClassLoader().getResourceAsStream("net/innig/macker/version.properties"));
					System.out.println("Macker " + p.get("macker.version.long"));
					System.out.println("http://innig.net/macker/");
					System.out.println("Licensed under GPL v2.1; see LICENSE.html");
					return;
				} else if ("-v".equals(args[arg]) || "--verbose".equals(args[arg])) {
					macker.setVerbose(true);
				} else if (args[arg].startsWith("-D") || "--define".equals(args[arg])) {
					int initialPos = 0;
					int equalPos;
					if (args[arg].length() == 2 || "--define".equals(args[arg])) {
						arg++;
					} else {
						initialPos = 2;
					}

					equalPos = args[arg].indexOf('=');
					if (equalPos == -1) {
						System.out.println("-D argument doesn't have name=value form: " + args[arg]);
						commandLineUsage();
						return;
					}
					final String varName = args[arg].substring(initialPos, equalPos);
					final String value = args[arg].substring(equalPos + 1);
					macker.setVariable(varName, value);
				} else if ("o".equals(args[arg]) || "--output".equals(args[arg])) {
					macker.setXmlReportFile(new File(args[++arg]));
				} else if ("--print-max".equals(args[arg])) {
					macker.setPrintMaxMessages(Integer.parseInt(args[++arg]));
				} else if ("--print".equals(args[arg])) {
					macker.setPrintThreshold(RuleSeverity.fromName(args[++arg]));
				} else if ("--anger".equals(args[arg])) {
					macker.setAngerThreshold(RuleSeverity.fromName(args[++arg]));
				} else if ("-r".equals(args[arg]) || "--rulesfile".equals(args[arg])) {
					nextIsRule = true;
				} else if (args[arg].startsWith("@")) {
					macker.addClassesFromFile(args[arg].substring(1));
					// the arg is a file with class names
				} else if (args[arg].endsWith(".xml") || nextIsRule) {
					macker.addRulesFile(new File(args[arg]));
					nextIsRule = false;
				} else if (args[arg].endsWith(".class")) {
					macker.addClass(new File(args[arg]));
				} else {
					System.out.println();
					System.out.println("macker: Unknown file type: " + args[arg]);
					System.out.println("(expected .class or .xml)");
					commandLineUsage();
					return;
				}
			}

			macker.check();

			if (!macker.hasRules() || !macker.hasClasses()) {
				commandLineUsage();
			}
		} catch (MackerIsMadException mime) {
			System.out.println(mime.getMessage());
			System.exit(2);
		} catch (IncompleteClassInfoException icie) {
			System.out.println(icie.getMessage());
			throw icie;
		} catch (Exception e) {
			e.printStackTrace(System.out);
			commandLineUsage();
			throw e;
		}
	}

	/**
	 * Display the command line usage and parameters on the {@link System.out} stream.
	 */
	public static void commandLineUsage() {
		System.out.println("usage: macker [opts]* <rules files> <classes> [@class list file]");
		System.out.println("          -r, --rulesfile <rules.xml>");
		System.out.println("          -o, --output <report.xml>");
		System.out.println("          -D, --define <var>=<value>");
		System.out.println("              --print <threshold>");
		System.out.println("              --anger <threshold>");
		System.out.println("              --print-max <max-messages>");
		System.out.println("          -v, --verbose");
		System.out.println("          -V, --version");
	}
	
	private RuleSeverity getAngerThreshold() {
		return this.angerThreshold;
	}
	
	private ClassManager getClassManager() {
		return this.classManager;
	}
	
	private List<MackerEventListener> getListeners() {
		return this.listeners;
	}
	
	private int getPrintMaxMessages() {
		return this.printMaxMessages;
	}
	
	private RuleSeverity getPrintThreshold() {
		return this.printThreshold;
	}
	
	private Collection<RuleSet> getRuleSets() {
		return this.ruleSets;
	}
	
	private Map<String, String> getVars() {
		return this.vars;
	}
	
	private boolean isVerbose() {
		return this.verbose;
	}
	
	private File getXmlReportFile() {
		return this.xmlReportFile;
	}
}
