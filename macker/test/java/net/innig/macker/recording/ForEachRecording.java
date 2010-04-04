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

package net.innig.macker.recording;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.innig.collect.CollectionDiff;
import net.innig.macker.event.ForEachEvent;
import net.innig.macker.event.ForEachFinished;
import net.innig.macker.event.ForEachIterationStarted;
import net.innig.macker.event.ForEachStarted;
import net.innig.macker.event.MackerEvent;

import org.jdom.Element;

public class ForEachRecording extends EventRecording {
	public ForEachRecording(EventRecording parent) {
		super(parent);
		iterations = new TreeMap<String, RuleSetRecording>();
	}

	public EventRecording record(MackerEvent event) {
		if (!(event instanceof ForEachEvent))
			return getParent().record(event);

		if (event instanceof ForEachStarted)
			var = ((ForEachEvent) event).getForEach().getVariableName();

		if (event instanceof ForEachIterationStarted) {
			String value = ((ForEachIterationStarted) event).getVariableValue();
			RuleSetRecording ruleSetRec = new RuleSetRecording(this);
			iterations.put(value, ruleSetRec);
			return ruleSetRec;
		}

		if (event instanceof ForEachFinished)
			return getParent();

		return this;
	}

	@SuppressWarnings("unchecked")
	public void read(Element elem) {
		var = elem.getAttributeValue("var");
		for (Element childElem : (List<Element>) elem.getChildren("iteration")) {
			String varValue = childElem.getAttributeValue("value");
			RuleSetRecording recording = new RuleSetRecording(this);
			recording.read(childElem);
			iterations.put(varValue, recording);
		}
	}

	public boolean compare(EventRecording actual, PrintWriter out) {
		if (!super.compare(actual, out))
			return false;

		boolean match = true;

		ForEachRecording actualForEach = (ForEachRecording) actual;
		if (!var.equals(actualForEach.var)) {
			out.println("Expected " + this + ", but got " + actual);
			match = false;
		}

		CollectionDiff<String> diff = new CollectionDiff<String>(iterations.keySet(), actualForEach.iterations.keySet());
		if (!diff.getRemoved().isEmpty())
			out.println(this + ": missing iterations: " + diff.getRemoved());
		if (!diff.getAdded().isEmpty())
			out.println(this + ": unexpected iterations: " + diff.getAdded());
		match = match && diff.getRemoved().isEmpty() && diff.getAdded().isEmpty();

		for (String varValue : diff.getSame()) {
			// out.println("(comparing " + var + "=" + varValue + ")");
			RuleSetRecording iterExpected = iterations.get(varValue);
			RuleSetRecording iterActual = actualForEach.iterations.get(varValue);
			match = iterExpected.compare(iterActual, out) && match;
		}
		return match;
	}

	public String toString() {
		return "[foreach:" + var + "]";
	}

	public void dump(PrintWriter out, int indent) {
		super.dump(out, indent);
		for (Map.Entry<String, RuleSetRecording> entry : iterations.entrySet()) {
			for (int n = -3; n < indent; n++)
				out.print(' ');
			out.println("[iteration:" + entry.getKey() + "]");
			entry.getValue().dump(out, indent + 6);
		}
	}

	private String var;
	private Map<String, RuleSetRecording> iterations;
}
