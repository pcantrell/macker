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

import net.innig.macker.rule.Rule;
import net.innig.macker.rule.RuleSeverity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

/**
 * @author Paul Cantrell
 */
public class MackerEvent extends EventObject {

	private static final long serialVersionUID = -8361984457903302252L;
	
	private final Rule rule;
	private final String description;
	private final List<String> messages;

	public MackerEvent(final Rule rule, final String description, final List<String> messages) {
		super(rule);
		this.rule = rule;
		this.description = description;
		this.messages = Collections.unmodifiableList(new ArrayList<String>(messages));
	}

	public Rule getRule() {
		return this.rule;
	}

	public String getDescription() {
		return this.description;
	}

	public List<String> getMessages() {
		return this.messages;
	}

	public String toString() {
		return getDescription();
	}

	public String toStringVerbose() {
		// ! This is completely crappy -- the PrintingListener probably should
		// be the one to deal with this
		final String cr = System.getProperty("line.separator");
		final StringBuffer s = new StringBuffer();
		if (getRule().getSeverity() != RuleSeverity.ERROR) {
			s.append(getRule().getSeverity().getName().toUpperCase());
			s.append(": ");
		}
		for (String msg : getMessages()) {
			s.append(msg);
			s.append(cr);
		}
		if (getDescription() != null) {
			s.append(getDescription());
		}
		s.append(cr);
		return s.toString();
	}
}
