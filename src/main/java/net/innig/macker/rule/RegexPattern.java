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

import net.innig.macker.structure.ClassInfo;

/**
 * @author Paul Cantrell
 */
public final class RegexPattern implements Pattern {
	// --------------------------------------------------------------------------
	// Constructors
	// --------------------------------------------------------------------------

	public RegexPattern(final String regexStr) throws MackerRegexSyntaxException {
		this.regex = new MackerRegex(regexStr);
	}

	// --------------------------------------------------------------------------
	// Properties
	// --------------------------------------------------------------------------

	public MackerRegex getRegex() {
		return this.regex;
	}

	private final MackerRegex regex;

	// --------------------------------------------------------------------------
	// Evaluation
	// --------------------------------------------------------------------------

	public boolean matches(final EvaluationContext context, final ClassInfo classInfo) throws RulesException {
		return getRegex().matches(context, classInfo.getFullClassName());
	}

	public String getMatch(final EvaluationContext context, final ClassInfo classInfo) throws RulesException {
		return getRegex().getMatch(context, classInfo.getFullClassName());
	}

	public String toString() {
		return getRegex().toString();
	}
}
