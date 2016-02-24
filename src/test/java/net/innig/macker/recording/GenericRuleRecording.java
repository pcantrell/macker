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

import net.innig.collect.CollectionDiff;
import net.innig.macker.event.AccessRuleViolation;
import net.innig.macker.event.MackerEvent;
import net.innig.macker.event.MessageEvent;
import net.innig.macker.rule.Rule;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * @author Paul Cantrell
 */
public class GenericRuleRecording extends EventRecording {

	private static final String DEFAULT_EVENT_PACKAGE = "net.innig.macker.event.";
	
	private Rule rule;
	private String eventType;
	private Set<Map<String, String>> events;
	
	public GenericRuleRecording(final EventRecording parent) {
		super(parent);
		this.events = new HashSet<Map<String, String>>();
	}

	public EventRecording record(final MackerEvent event) {
		if (getRule() == null) {
			setRule(event.getRule());
		}
		
		if (event.getRule() != getRule()) {
			return getParent().record(event);
		}

		final Map<String, String> eventAttributes = new TreeMap<String, String>();
		setEventType(event.getClass().getName());
		if (getEventType().startsWith(DEFAULT_EVENT_PACKAGE)) {
			setEventType(getEventType().substring(DEFAULT_EVENT_PACKAGE.length()));
		}
		eventAttributes.put("type", getEventType());
		eventAttributes.put("severity", event.getRule().getSeverity().getName());
		final int msgNum = 0;
		for (String msg : event.getMessages()) {
			eventAttributes.put("message" + msgNum, msg);
		}

		if (!(event instanceof MessageEvent)) {
			if (event instanceof AccessRuleViolation) {
				final AccessRuleViolation arv = (AccessRuleViolation) event;
				eventAttributes.put("from", arv.getFrom().getFullClassName());
				eventAttributes.put("to", arv.getTo().getFullClassName());
			} else {
				throw new IllegalArgumentException("Unknown event type: " + event);
			}
		}

		getEvents().add(eventAttributes);

		return this;
	}

	@SuppressWarnings("unchecked")
	public void read(final Element elem) {
		final Map<String, String> baseAtt = getAttributeValueMap(elem);
		for (Element eventElem : (List<Element>) elem.getChildren("event")) {
			final Map<String, String> eventAtt = new TreeMap<String, String>(baseAtt);
			eventAtt.putAll(getAttributeValueMap(eventElem));
			setEventType(eventAtt.get("type"));
			getEvents().add(eventAtt);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> getAttributeValueMap(final Element elem) {
		final Map<String, String> attValues = new TreeMap<String, String>();
		for (Attribute attr : (List<Attribute>) elem.getAttributes()) {
			attValues.put(attr.getName(), attr.getValue());
		}
		return attValues;
	}

	public boolean compare(final EventRecording actual, final PrintWriter out) {
		if (!super.compare(actual, out)) {
			return false;
		}

		boolean match = true;
		final GenericRuleRecording actualGRR = (GenericRuleRecording) actual;
		final Set<Map<String, String>> expectedSet = getEvents();
		final Set<Map<String, String>> actualSet = actualGRR.events;
		final CollectionDiff<Map<String, String>> diff =
			new CollectionDiff<Map<String, String>>(expectedSet, actualSet);
		if (!diff.getRemoved().isEmpty()) {
			out.println(this + ": missing events:");
			dump(out, diff.getRemoved());
			match = false;
		}
		if (!diff.getAdded().isEmpty()) {
			out.println(this + ": unexpected events:");
			dump(out, diff.getAdded());
			match = false;
		}
		return match;
	}

	private void dump(final PrintWriter out, final Collection<?> events) {
		for (Object event : events) {
			out.println("    " + event);
		}
	}

	public String toString() {
		return "[rule:" + getEventType() + "]";
	}

	public void dump(final PrintWriter out, final int indent) {
		super.dump(out, indent);
		for (Map<String, String> event : getEvents()) {
			for (int n = -3; n < indent; n++) {
				out.print(' ');
			}
			out.println(event);
		}
	}
	
	private Set<Map<String, String>> getEvents() {
		return this.events;
	}
	
	private String getEventType() {
		return this.eventType;
	}
	
	private void setEventType(final String eventType) {
		this.eventType = eventType;
	}
	
	private Rule getRule() {
		return this.rule;
	}
	
	private void setRule(final Rule rule) {
		this.rule = rule;
	}
}
