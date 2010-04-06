package net.innig.macker.event;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;

import net.innig.macker.rule.RuleSet;
import net.innig.macker.structure.ClassInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @author Paul Cantrell
 */
public class XmlReportingListener implements MackerEventListener {
	
	private Writer writer;
	private String encoding;

	private Document document;
	private Element curElem;
	private LinkedList<Element> elemStack;

	public XmlReportingListener(final File outFile) throws ListenerException {
		try {
			if (outFile.exists()) {
				final boolean succesfullyDeleted = outFile.delete();
				if (!succesfullyDeleted) {
					throw new ListenerException(this, "Unable to remove report file \"" + outFile + "\"");
				}
			}
			final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8");
			final BufferedWriter bufferedOut = new BufferedWriter(out);
			init(bufferedOut, "UTF-8");
		} catch (IOException ioe) {
			throw new ListenerException(this, "Unable to remove and re-create report file \"" + outFile + "\"", ioe);
		}
	}

	public XmlReportingListener(final Writer out, final String encoding) throws ListenerException {
		init(out, encoding);
	}

	private void init(final Writer writer, final String encoding) throws ListenerException {
		this.writer = writer;
		this.encoding = encoding;

		setElemStack(new LinkedList<Element>());
		final Element topElem = new Element("macker-report");
		final Element timestampElem = new Element("timestamp");
		timestampElem.setText(new java.util.Date().toString());
		// to heck with
		// sophisticated
		// localization!
		topElem.addContent(timestampElem);

		pushElem(topElem);
		setDocument(new Document(topElem));
	}

	public void flush() throws ListenerException {
		try {
			final Format format = Format.getPrettyFormat();
			format.setEncoding(getEncoding());
			final XMLOutputter xmlOut = new XMLOutputter(format);
			xmlOut.output(getDocument(), getWriter());
			getWriter().flush();
		} catch (IOException ioe) {
			throw new ListenerException(this, "Unable to write XML report", ioe);
		}
	}

	public void close() throws ListenerException {
		try {
			getWriter().close();
		} catch (IOException ioe) {
			throw new ListenerException(this, "Unable to close XML report", ioe);
		}
	}

	public void mackerStarted(final RuleSet ruleSet) {
		if (ruleSet.hasName()) {
			final Element ruleSetElem = new Element("ruleset");
			ruleSetElem.setAttribute("name", ruleSet.getName());
			getCurElem().addContent(ruleSetElem);
			pushElem(ruleSetElem);
		} else {
			// push again so finish can pop
			pushElem(getCurElem());
		}
	}

	public void mackerFinished(final RuleSet ruleSet) throws MackerIsMadException, ListenerException {
		popElem();
	}

	public void mackerAborted(final RuleSet ruleSet) {
		setCurElem(null);
	}

	public void handleMackerEvent(final RuleSet ruleSet, final MackerEvent event) throws MackerIsMadException {
		if (event instanceof MessageEvent) {
			final Element messageRuleElem = new Element("message-rule");
			handleEventBasics(messageRuleElem, event);
			getCurElem().addContent(messageRuleElem);
		}

		if (event instanceof AccessRuleViolation) {
			final AccessRuleViolation violation = (AccessRuleViolation) event;
			final Element violationElem = new Element("access-rule-violation");

			handleEventBasics(violationElem, violation);

			final Element fromElem = new Element("from");
			final Element toElem = new Element("to");
			describeClass(fromElem, violation.getFrom());
			describeClass(toElem, violation.getTo());
			violationElem.addContent(fromElem);
			violationElem.addContent(toElem);

			getCurElem().addContent(violationElem);
		}

		if (event instanceof ForEachStarted) {
			final ForEachStarted forEachStarted = (ForEachStarted) event;
			final Element forEachElem = new Element("foreach");
			forEachElem.setAttribute("var", forEachStarted.getForEach().getVariableName());
			getCurElem().addContent(forEachElem);
			pushElem(forEachElem);
		}

		if (event instanceof ForEachIterationStarted) {
			final ForEachIterationStarted forEachIter = (ForEachIterationStarted) event;
			final Element iterElem = new Element("iteration");
			iterElem.setAttribute("value", forEachIter.getVariableValue());
			getCurElem().addContent(iterElem);
			pushElem(iterElem);
		}

		if (event instanceof ForEachIterationFinished || event instanceof ForEachFinished) {
			popElem();
		}
	}

	private void handleEventBasics(final Element elem, final MackerEvent event) {
		elem.setAttribute("severity", event.getRule().getSeverity().getName());
		for (String message : event.getMessages()) {
			final Element messageElem = new Element("message");
			messageElem.setText(message);
			elem.addContent(messageElem);
		}
	}

	private void describeClass(final Element classInfoElem, final ClassInfo classInfo) {
		final Element fullElem = new Element("full-name");
		final Element classElem = new Element("class");
		final Element packElem = new Element("package");
		fullElem.setText(classInfo.getFullName());
		classElem.setText(classInfo.getClassName());
		packElem.setText(classInfo.getPackageName());
		classInfoElem.addContent(fullElem);
		classInfoElem.addContent(classElem);
		if (!StringUtils.isEmpty(classInfo.getPackageName())) {
			classInfoElem.addContent(packElem);
		}
	}

	private void pushElem(final Element elem) {
		getElemStack().addLast(getCurElem());
		setCurElem(elem);
	}

	private void popElem() {
		setCurElem(getElemStack().removeLast());
	}

	public String toString() {
		return "XmlReportingListener";
	}
	
	private Element getCurElem() {
		return this.curElem;
	}
	
	private void setCurElem(final Element curElem) {
		this.curElem = curElem;
	}
	
	private Document getDocument() {
		return this.document;
	}
	
	private void setDocument(final Document document) {
		this.document = document;
	}
	
	private LinkedList<Element> getElemStack() {
		return this.elemStack;
	}
	
	private void setElemStack(final LinkedList<Element> elemStack) {
		this.elemStack = elemStack;
	}
	
	private String getEncoding() {
		return this.encoding;
	}
	
	private Writer getWriter() {
		return this.writer;
	}
}
