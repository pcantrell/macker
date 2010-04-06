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

package net.innig.macker.rule.filter;

import net.innig.macker.rule.EvaluationContext;
import net.innig.macker.rule.Pattern;
import net.innig.macker.rule.RuleSet;
import net.innig.macker.rule.RulesException;
import net.innig.macker.structure.ClassInfo;

import java.util.List;
import java.util.Map;

/**
 * @author Paul Cantrell
 */
public class InterfaceFilter implements Filter {
	
	public Pattern createPattern(final RuleSet ruleSet, final List<Pattern> params, final Map<String, String> options)
			throws RulesException {
		if (params.size() != 0) {
			throw new FilterSyntaxException(this, "Filter \"" + options.get("filter")
					+ "\" expects no parameters, but has " + params.size());
		}
		return INTERFACE_PATTERN;
	}

	private static final Pattern INTERFACE_PATTERN = new Pattern() {
		public boolean matches(final EvaluationContext context, final ClassInfo classInfo) throws RulesException {
			return classInfo.isInterface();
		}
	};
}
