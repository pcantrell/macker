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

package net.innig.macker.rule;

import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.structure.ClassManager;

/**
 * @author Paul Cantrell
 */
public abstract class Rule {
	
	private final RuleSet parent;
	private RuleSeverity severity;
	
	public Rule(final RuleSet parent) {
		this.parent = parent;
	}

	public RuleSet getParent() {
		return this.parent;
	}

	public RuleSeverity getSeverity() {
		if (this.severity != null) {
			return this.severity;
		}
		
		if (getParent() != null) {
			return getParent().getSeverity();
		}

		return RuleSeverity.ERROR;
	}

	public void setSeverity(final RuleSeverity severity) {
		this.severity = severity;
	}

	public abstract void check(EvaluationContext context, ClassManager classes) throws RulesException,
			MackerIsMadException, ListenerException;
}
