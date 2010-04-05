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
	private ClassManager cm;
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
		cm = new ClassManager();
		ruleSets = new ArrayList<RuleSet>();
		vars = new HashMap<String, String>();
		verbose = false;
	}

	/**
	 * Add a primary class to check to the class manager.
	 * 
	 * @param classFile The class to check.
	 * 
	 * @throws IOException When reading the class failed.
	 * @throws ClassParseException When the class couldn't be parsed.
	 */
	public void addClass(File classFile) throws IOException, ClassParseException {
		cm.makePrimary(cm.readClass(classFile));
	}

	/**
	 * Add a primary class to check to the class manager.
	 * 
	 * @param classFile The class to check.
	 * 
	 * @throws IOException When reading the class failed.
	 * @throws ClassParseException When the class couldn't be parsed.
	 */
	public void addClass(InputStream classFile) throws IOException, ClassParseException {
		cm.makePrimary(cm.readClass(classFile));
	}

	/**
	 * Add a primary class to check to the class manager.
	 * 
	 * @param classFile The name of the class to check.
	 * 
	 * @throws IOException When reading the class failed.
	 * @throws ClassParseException When the class couldn't be parsed.
	 */
	public void addClass(String className) throws ClassNotFoundException {
		cm.makePrimary(cm.getClassInfo(className));
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
	public void addClassesFromFile(String fileName) throws IOException, ClassParseException {
		File indexFile = new File(fileName);
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
	public void addReachableClasses(Class<?> initialClass, final String primaryPrefix)
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
	public void addReachableClasses(String initialClassName, final String primaryPrefix)
			throws IncompleteClassInfoException {
		Graphs.reachableNodes(cm.getClassInfo(initialClassName), new GraphWalker<ClassInfo>() {
			public Collection<ClassInfo> getEdgesFrom(ClassInfo classInfo) {
				cm.makePrimary(classInfo);
				return InnigCollections.select(classInfo.getReferences().keySet(), new Selector<ClassInfo>() {
					public boolean select(ClassInfo classInfo) {
						return classInfo.getFullName().startsWith(primaryPrefix);
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
		return !cm.getPrimaryClasses().isEmpty();
	}

	/**
	 * Add a file containing the rules to use.
	 * 
	 * @param rulesFile The rules file.
	 * 
	 * @throws IOException When reading the file failed.
	 * @throws RulesException When the rules could not be build.
	 */
	public void addRulesFile(File rulesFile) throws IOException, RulesException {
		ruleSets.addAll(new RuleSetBuilder().build(rulesFile));
	}

	/**
	 * Add a file containing the rules to use.
	 * 
	 * @param rulesFile {@link InputStream} to the rules file.
	 * 
	 * @throws IOException When reading the file failed.
	 * @throws RulesException When the rules could not be build.
	 */
	public void addRulesFile(InputStream rulesFile) throws IOException, RulesException {
		ruleSets.addAll(new RuleSetBuilder().build(rulesFile));
	}

	/**
	 * Add a {@link RuleSet} for execution on the classes.
	 * 
	 * @param ruleSet The {@link RuleSet} to load.
	 * 
	 * @throws IOException When the ruleset could not be loaded.
	 * @throws RulesException When the ruleset was invalid.
	 */
	public void addRuleSet(RuleSet ruleSet) throws IOException, RulesException {
		ruleSets.add(ruleSet);
	}

	/**
	 * Add an {@link MackerEventListener} for handling {@link MackerEvent} events.
	 * 
	 * @param listener The {@link MackerEventListener}.
	 */
	public void addListener(MackerEventListener listener) {
		listeners.add(listener);
	}

	/**
	 * Check if there is any {@link RuleSet} loaded.
	 * 
	 * @return <code>true</code> if there is atleast 1 {@link RuleSet} loaded.
	 */
	public boolean hasRules() {
		return !ruleSets.isEmpty();
	}

	/**
	 * Set a Macker variable.
	 * 
	 * @param name The name of the variable.
	 * @param value The value of the variable.
	 */
	public void setVariable(String name, String value) {
		vars.put(name, value);
	}

	/**
	 * Indicate if Macker should be verbose in it's output.
	 * 
	 * @param verbose <code>true</code> for verbose output.
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Set the ClassLoader to use.
	 * 
	 * @param classLoader The ClassLoader.
	 */
	public void setClassLoader(ClassLoader classLoader) {
		cm.setClassLoader(classLoader);
	}

	/**
	 * Set the maximum number of messages to show.
	 * 
	 * @param printMaxMessages The maximum number of messages.
	 */
	public void setPrintMaxMessages(int printMaxMessages) {
		this.printMaxMessages = printMaxMessages;
	}

	/**
	 * Set the lowest {@link RuleSeverity} level to show output from.
	 * 
	 * @param printThreshold The lowest {@link RuleSeverity} level to show output from.
	 */
	public void setPrintThreshold(RuleSeverity printThreshold) {
		this.printThreshold = printThreshold;
	}

	/**
	 * Set the lowest {@link RuleSeverity} level to cause Macker to report an error.
	 * 
	 * @param angerThreshold The lowest {@link RuleSeverity} level to report an error from.
	 */
	public void setAngerThreshold(RuleSeverity angerThreshold) {
		this.angerThreshold = angerThreshold;
	}

	/**
	 * Set the XML report file to use.
	 * 
	 * @param xmlReportFile The xml report file to use.
	 */
	public void setXmlReportFile(File xmlReportFile) {
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

		if (verbose) {
			System.out.println(cm.getPrimaryClasses().size() + " primary classes");
			System.out.println(cm.getAllClasses().size() + " total classes");
			System.out.println(cm.getReferences().size() + " references");

			for (ClassInfo classInfo : cm.getPrimaryClasses()) {
				System.out.println("Classes used by " + classInfo + ":");
				for (ClassInfo used : classInfo.getReferences().keySet()) {
					System.out.println("    " + used);
				}
				System.out.println();
			}
		}

		PrintingListener printing;
		if (printThreshold == null) {
			printing = null;
		} else {
			printing = new PrintingListener(System.out);
			printing.setThreshold(printThreshold);
			if (printMaxMessages > 0) {
				printing.setMaxMessages(printMaxMessages);
			}
			addListener(printing);
		}

		ThrowingListener throwing;
		if (angerThreshold == null) {
			throwing = null;
		} else {
			throwing = new ThrowingListener(null, angerThreshold);
			addListener(throwing);
		}

		XmlReportingListener xmlReporting = null;
		if (xmlReportFile != null) {
			xmlReporting = new XmlReportingListener(xmlReportFile);
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
		for (RuleSet rs : ruleSets) {
			if (verbose) {
				for (final Pattern pat : rs.getAllPatterns()) {
					final EvaluationContext ctx = new EvaluationContext(cm, rs);
					System.out.println("matching " + pat);
					for (ClassInfo classInfo : cm.getPrimaryClasses()) {
						if (pat.matches(ctx, classInfo)) {
							System.out.println("    " + classInfo);
						}
					}
					System.out.println();
				}
			}

			EvaluationContext context = new EvaluationContext(cm, rs);
			context.setVariables(vars);
			for (MackerEventListener listener : listeners) {
				context.addListener(listener);
			}

			rs.check(context, cm);
		}
	}

	/**
	 * Main class entry point for command line execution.
	 * 
	 * @param args The command line arguments.
	 * 
	 * @throws Exception When execution fails.
	 */
	public static void main(String[] args) throws Exception {
		try {
			// Parse args
			Macker macker = new Macker();

			boolean nextIsRule = false;
			for (int arg = 0; arg < args.length; arg++) {
				if (args[arg].equals("-h") || args[arg].equals("-help") || args[arg].equals("--help")) {
					commandLineUsage();
					return;
				} else if (args[arg].equals("-V") || args[arg].equals("--version")) {
					Properties p = new Properties();
					p.load(Macker.class.getClassLoader().getResourceAsStream("net/innig/macker/version.properties"));
					System.out.println("Macker " + p.get("macker.version.long"));
					System.out.println("http://innig.net/macker/");
					System.out.println("Licensed under GPL v2.1; see LICENSE.html");
					return;
				} else if (args[arg].equals("-v") || args[arg].equals("--verbose")) {
					macker.setVerbose(true);
				} else if (args[arg].startsWith("-D") || args[arg].equals("--define")) {
					int initialPos = 0;
					int equalPos;
					if (args[arg].length() == 2 || args[arg].equals("--define")) {
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
					String varName = args[arg].substring(initialPos, equalPos);
					String value = args[arg].substring(equalPos + 1);
					macker.setVariable(varName, value);
				} else if (args[arg].equals("-o") || args[arg].equals("--output")) {
					macker.setXmlReportFile(new File(args[++arg]));
				} else if (args[arg].equals("--print-max")) {
					macker.setPrintMaxMessages(Integer.parseInt(args[++arg]));
				} else if (args[arg].equals("--print")) {
					macker.setPrintThreshold(RuleSeverity.fromName(args[++arg]));
				} else if (args[arg].equals("--anger")) {
					macker.setAngerThreshold(RuleSeverity.fromName(args[++arg]));
				} else if (args[arg].equals("-r") || args[arg].equals("--rulesfile")) {
					nextIsRule = true;
				} else if (args[arg].startsWith("@")) {
					macker.addClassesFromFile(args[arg].substring(1)); // the
				// arg is a file with class names
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
}
