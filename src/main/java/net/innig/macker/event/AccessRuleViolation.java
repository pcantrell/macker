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

import net.innig.macker.rule.AccessRule;
import net.innig.macker.structure.ClassInfo;

import java.util.List;

/**
 * @author Paul Cantrell
 */
public class AccessRuleViolation extends MackerEvent {
	
	private static final long serialVersionUID = -2600252287009857450L;
	
	private static final String CR = System.getProperty("line.separator");

	private final AccessRule accessRule;
	private final ClassInfo from;
	private final ClassInfo to;
	
	public AccessRuleViolation(final AccessRule accessRule, final ClassInfo from,
			final ClassInfo to, final List<String> messages) {
		super(accessRule, "Illegal reference" + CR // ! hokey, hokey, hokey!
				+ "  from " + from + CR + "    to " + to, messages);
		this.accessRule = accessRule;
		this.from = from;
		this.to = to;
	}

	public final AccessRule getAccessRule() {
		return this.accessRule;
	}

	public final ClassInfo getFrom() {
		return this.from;
	}

	public final ClassInfo getTo() {
		return this.to;
	}
}
