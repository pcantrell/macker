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
import net.innig.macker.structure.ClassInfo;
import net.innig.macker.structure.ClassManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Paul Cantrell
 */
public class RuleSet extends Rule {
	
	private final Map<String, Pattern> patterns;
	private final Collection<Rule> rules;
	private String name;
	private Pattern subsetPat;
	
	public static RuleSet getMackerDefaults() {
		if (defaults == null) {
			try {
				defaults = new RuleSet();
				defaults.setPattern("from", new RegexPattern("${from-full}"));
				defaults.setPattern("to", new RegexPattern("${to-full}"));
			} catch (MackerRegexSyntaxException mrse) {
				throw new RuntimeException("Macker built-ins are broken", mrse);
			} // ! what else to throw?
		}
		return defaults;
	}

	private static RuleSet defaults;

	public RuleSet(final RuleSet parent) {
		super(parent);
		if (parent == null) {
			throw new IllegalArgumentException("parent == null");
		}

		this.rules = new ArrayList<Rule>();
		this.patterns = new HashMap<String, Pattern>();
	}

	private RuleSet() {
		super(null);
		this.rules = Collections.emptyList();
		this.patterns = new HashMap<String, Pattern>();
	}

	public String getName() {
		if (this.name == null) {
			if (getParent() == null) {
				return "<anonymous ruleset>";
			}
			
			return getParent().getName();
		}

		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public boolean hasName() {
		return getName() != null;
	}

	public boolean declaresPattern(final String name) {
		return getPatterns().keySet().contains(name);
	}

	public Pattern getPattern(final String name) {
		final Pattern pat = getPatterns().get(name);
		if (pat != null) {
			return pat;
		}
		if (getParent() != null) {
			return getParent().getPattern(name);
		}
		return null;
	}

	public void setPattern(final String name, final Pattern pattern) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null");
		}
		if (pattern == null) {
			throw new IllegalArgumentException("pattern cannot be null");
		}
		getPatterns().put(name, pattern);
	}

	public Collection<Pattern> getAllPatterns() {
		return getPatterns().values();
	}

	public void clearPattern(final String name) {
		getPatterns().remove(name);
	}

	public Collection<Rule> getRules() {
		return this.rules;
	}

	public void addRule(final Rule rule) {
		getRules().add(rule);
	}

	public Pattern getSubsetPattern() {
		return this.subsetPat;
	}

	public void setSubsetPattern(final Pattern subsetPat) {
		this.subsetPat = subsetPat;
	}

	public boolean isInSubset(final EvaluationContext context, final ClassInfo classInfo) throws RulesException {
		if (getSubsetPattern() != null && !getSubsetPattern().matches(context, classInfo)) {
			return false;
		}
		if (getParent() != null) {
			return getParent().isInSubset(context, classInfo);
		}
		return true;
	}

	public void check(final EvaluationContext parentContext, final ClassManager classes) throws RulesException,
			MackerIsMadException, ListenerException {
		final EvaluationContext context = new EvaluationContext(this, parentContext);
		context.broadcastStarted();
		boolean finished = false;
		try {
			for (Rule rule : getRules()) {
				rule.check(context, classes);
			}
			context.broadcastFinished();
			finished = true;
		} finally {
			if (!finished) {
				context.broadcastAborted();
			}
		}
	}

	public String toString() {
		return getClass().getName() + '[' + getName() + ", parent=" + getParent() + ']';
	}
	
	private Map<String, Pattern> getPatterns() {
		return this.patterns;
	}
}
