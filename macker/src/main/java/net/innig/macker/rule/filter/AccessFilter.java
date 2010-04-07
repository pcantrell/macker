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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.innig.macker.rule.EvaluationContext;
import net.innig.macker.rule.Pattern;
import net.innig.macker.rule.RuleSet;
import net.innig.macker.rule.RulesException;
import net.innig.macker.structure.AccessModifier;
import net.innig.macker.structure.ClassInfo;

/**
 * @author Paul Cantrell
 */
public class AccessFilter implements Filter {
	public Pattern createPattern(final RuleSet ruleSet, final List<Pattern> params, final Map<String, String> options)
			throws RulesException {
		if (!params.isEmpty()) {
			throw new FilterSyntaxException(this, "Filter \"" + options.get("filter")
					+ "\" expects no parameters, but has " + params.size());
		}

		final String maxS = options.get("max");
		final String minS = options.get("min");
		final AccessModifier max = getAccessModifier(maxS, AccessModifier.PUBLIC);
		final AccessModifier min = getAccessModifier(minS, AccessModifier.PRIVATE);

		if (maxS == null && minS == null) {
			throw new FilterSyntaxException(this, options.get("filter")
					+ " requires a \"max\" or \"min\" option (or both)");
		}
		if (max == null && maxS != null) {
			throw new FilterSyntaxException(this, '"' + maxS + "\" is not a valid access level; expected one of: "
					+ Arrays.asList(AccessModifier.values()));
		}
		if (min == null && minS != null) {
			throw new FilterSyntaxException(this, '"' + minS + "\" is not a valid access level; expected one of: "
					+ Arrays.asList(AccessModifier.values()));
		}

		return new AccessModifierPattern(min, max);
	}
	
	private AccessModifier getAccessModifier(final String value, final AccessModifier valueIfNull) {
		if (value == null) {
			return valueIfNull;
		}
		
		return AccessModifier.valueOf(value.toUpperCase());
	}
	
	/** Match AccessModifier Pattern. */
	private static class AccessModifierPattern implements Pattern {
		
		private final AccessModifier min;
		private final AccessModifier max;
		
		public AccessModifierPattern(final AccessModifier min, final AccessModifier max) {
			this.min = min;
			this.max = max;
		}
		
		public boolean matches(final EvaluationContext context, final ClassInfo classInfo) throws RulesException {
			return classInfo.getAccessModifier().compareTo(this.min) >= 0
					&& classInfo.getAccessModifier().compareTo(this.max) <= 0;
		}
	}
}
