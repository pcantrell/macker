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

import net.innig.macker.event.ForEachFinished;
import net.innig.macker.event.ForEachIterationFinished;
import net.innig.macker.event.ForEachIterationStarted;
import net.innig.macker.event.ForEachStarted;
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.structure.ClassInfo;
import net.innig.macker.structure.ClassManager;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Paul Cantrell
 */
public class ForEach extends Rule {

	private RuleSet ruleSet;
	private String variableName;
	private String regexS;
	private RegexPattern regexPat;
	
	public ForEach(final RuleSet parent) {
		super(parent);
	}

	public String getVariableName() {
		return this.variableName;
	}

	public void setVariableName(final String variableName) {
		this.variableName = variableName;
	}

	public String getRegex() {
		return this.regexS;
	}

	public void setRegex(final String regexS) throws MackerRegexSyntaxException {
		this.regexS = regexS;
		setRegexPat(new RegexPattern(regexS));
	}

	public RuleSet getRuleSet() {
		return this.ruleSet;
	}

	public void setRuleSet(final RuleSet ruleSet) {
		this.ruleSet = ruleSet;
	}

	public void check(final EvaluationContext parentContext, final ClassManager classes) throws RulesException,
			MackerIsMadException, ListenerException {
		final EvaluationContext context = new EvaluationContext(getRuleSet(), parentContext);

		final Set<String> varValues = new TreeSet<String>();
		final Set<ClassInfo> pool = new HashSet<ClassInfo>();
		for (ClassInfo curClass : classes.getPrimaryClasses()) {
			if (getParent().isInSubset(context, curClass)) {
				pool.add(curClass);
				for (ClassInfo referencedClass : curClass.getReferences().keySet()) {
					pool.add(referencedClass);
				}
			}
		}

		for (ClassInfo classInfo : pool) {
			final String varValue = getRegexPat().getMatch(parentContext, classInfo);
			if (varValue != null) {
				varValues.add(varValue);
			}
		}

		context.broadcastEvent(new ForEachStarted(this));
		for (String varValue : varValues) {
			context.broadcastEvent(new ForEachIterationStarted(this, varValue));

			context.setVariableValue(getVariableName(), varValue);
			getRuleSet().check(context, classes);

			context.broadcastEvent(new ForEachIterationFinished(this, varValue));
		}
		context.broadcastEvent(new ForEachFinished(this));
	}

	private RegexPattern getRegexPat() {
		return this.regexPat;
	}
	
	private void setRegexPat(final RegexPattern regexPat) {
		this.regexPat = regexPat;
	}
}
