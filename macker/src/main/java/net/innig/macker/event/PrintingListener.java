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

package net.innig.macker.event;

import net.innig.collect.CompositeMultiMap;
import net.innig.collect.MultiMap;
import net.innig.macker.rule.RuleSet;
import net.innig.macker.rule.RuleSeverity;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Paul Cantrell
 */
public class PrintingListener implements MackerEventListener {

	public PrintingListener(final PrintWriter out) {
		this.writer = out;
	}

	public PrintingListener(final Writer out) {
		this.writer = new PrintWriter(out, true);
	}

	public PrintingListener(final OutputStream out) {
		this.writer = new PrintWriter(out, true);
	}

	public void setThreshold(final RuleSeverity threshold) {
		this.threshold = threshold;
	}
	
	public void setMaxMessages(final int maxMessages) {
		this.maxMessages = maxMessages;
	}

	public void mackerStarted(final RuleSet ruleSet) {
		if (ruleSet.getParent() == null || ruleSet.hasName()) {
			getWriter().println();
			getWriter().println("(Checking ruleset: " + ruleSet.getName() + " ...)");
			setFirst(true);
		}
	}

	public void mackerFinished(final RuleSet ruleSet) throws MackerIsMadException {
	}

	public void mackerAborted(final RuleSet ruleSet) {
	} // don't care

	public void handleMackerEvent(final RuleSet ruleSet, final MackerEvent event) throws MackerIsMadException {
		if (event instanceof ForEachEvent) {
			if (event instanceof ForEachIterationStarted) {
				final ForEachIterationStarted iterStart = (ForEachIterationStarted) event;
				getWriter().print('(');
				getWriter().print(iterStart.getForEach().getVariableName());
				getWriter().print(": ");
				getWriter().print(iterStart.getVariableValue());
				getWriter().println(")");
			}
			// ignore other ForEachEvents
		} else {
			getEventsBySeverity().put(event.getRule().getSeverity(), event);
			if (event.getRule().getSeverity().compareTo(getThreshold()) >= 0) {
				if (getMessagesPrinted() < getMaxMessages()) {
					if (isFirst()) {
						getWriter().println();
						setFirst(false);
					}
					getWriter().println(event.toStringVerbose());
				}
				if (getMessagesPrinted() == getMaxMessages()) {
					getWriter().println("WARNING: Exceeded the limit of " + getMaxMessages() + " message"
							+ (getMaxMessages() == 1 ? "" : "s") + "; further messages surpressed");
				}
				this.messagesPrinted++;
			}

		}
	}

	public void printSummary() {
		// output looks like: "(2 errors, 1 warning)"
		boolean firstSeverity = true;
		final List<RuleSeverity> severities = new ArrayList<RuleSeverity>(getEventsBySeverity().keySet());
		Collections.reverse(severities);
		for (RuleSeverity severity : severities) {
			final Collection<MackerEvent> eventsForSev = getEventsBySeverity().get(severity);
			if (eventsForSev.size() > 0) {
				if (firstSeverity) {
					getWriter().print("(");
				} else {
					getWriter().print(", ");
				}
				firstSeverity = false;
				getWriter().print(eventsForSev.size());
				getWriter().print(' ');
				getWriter().print((eventsForSev.size() == 1) ? severity.getName() : severity.getNamePlural());
			}
		}
		if (!firstSeverity) {
			getWriter().println(')');
		}
	}

	public String toString() {
		return "PrintingListener";
	}
	
	private MultiMap<RuleSeverity, MackerEvent> getEventsBySeverity() {
		return this.eventsBySeverity;
	}
	
	private boolean isFirst() {
		return this.first;
	}
	
	private void setFirst(final boolean first) {
		this.first = first;
	}
	
	private int getMaxMessages() {
		return this.maxMessages;
	}
	
	private int getMessagesPrinted() {
		return this.messagesPrinted;
	}
	
	private RuleSeverity getThreshold() {
		return this.threshold;
	}
	
	private PrintWriter getWriter() {
		return this.writer;
	}

	private boolean first;
	private PrintWriter writer;
	private int maxMessages = Integer.MAX_VALUE;
	private int messagesPrinted = 0;
	private RuleSeverity threshold = RuleSeverity.INFO;
	private final MultiMap<RuleSeverity, MackerEvent> eventsBySeverity =
		new CompositeMultiMap<RuleSeverity, MackerEvent>(
			new EnumMap<RuleSeverity, Set<MackerEvent>>(RuleSeverity.class), HashSet.class);
}
