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

import net.innig.collect.MultiMap;
import net.innig.macker.event.AccessRuleViolation;
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.structure.ClassInfo;
import net.innig.macker.structure.ClassManager;
import net.innig.macker.util.IncludeExcludeLogic;
import net.innig.macker.util.IncludeExcludeNode;

import java.util.Collections;
import java.util.List;

/**
 * @author Paul Cantrell
 */
public class AccessRule extends Rule {
	// --------------------------------------------------------------------------
	// Constructors
	// --------------------------------------------------------------------------

	public AccessRule(final RuleSet parent) {
		super(parent);
		this.type = AccessRuleType.DENY;
		this.from = Pattern.ALL;
		this.to = Pattern.ALL;
	}

	// --------------------------------------------------------------------------
	// Properties
	// --------------------------------------------------------------------------

	public AccessRuleType getType() {
		return this.type;
	}

	public void setType(final AccessRuleType type) {
		if (type == null) {
			throw new IllegalArgumentException("type parameter cannot be null");
		}
		this.type = type;
	}

	public Pattern getFrom() {
		return this.from;
	}

	public void setFrom(final Pattern from) {
		this.from = from;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public Pattern getTo() {
		return this.to;
	}

	public void setTo(final Pattern to) {
		this.to = to;
	}

	public AccessRule getChild() {
		return this.child;
	}

	public void setChild(final AccessRule child) {
		this.child = child;
	}

	public AccessRule getNext() {
		return this.next;
	}

	public void setNext(final AccessRule next) {
		this.next = next;
	}

	private AccessRuleType type;
	private Pattern from;
	private Pattern to;
	private String message;
	private AccessRule child;
	private AccessRule next;

	// --------------------------------------------------------------------------
	// Evaluation
	// --------------------------------------------------------------------------

	public void check(final EvaluationContext context, final ClassManager classes)
			throws RulesException, MackerIsMadException, ListenerException {
		final EvaluationContext localContext = new EvaluationContext(context);
		for (MultiMap.Entry<ClassInfo, ClassInfo> reference : classes.getReferences().entrySet()) {
			final ClassInfo from = reference.getKey();
			final ClassInfo to = reference.getValue();
			if (from.equals(to)) {
				continue;
			}
			if (!localContext.getRuleSet().isInSubset(localContext, from)) {
				continue;
			}

			localContext.setVariableValue("from", from.getClassName());
			localContext.setVariableValue("to", to.getClassName());
			localContext.setVariableValue("from-package", from.getPackageName());
			localContext.setVariableValue("to-package", to.getPackageName());
			localContext.setVariableValue("from-full", from.getFullClassName());
			localContext.setVariableValue("to-full", to.getFullClassName());

			if (!checkAccess(localContext, from, to)) {
				List<String> messages;
				if (getMessage() == null) {
					messages = Collections.emptyList();
				} else {
					messages = Collections.singletonList(VariableParser.parse(localContext, getMessage()));
				}
				context.broadcastEvent(new AccessRuleViolation(this, from, to, messages));
			}
		}
	}

	private boolean checkAccess(final EvaluationContext context, final ClassInfo fromClass, final ClassInfo toClass)
			throws RulesException {
		return IncludeExcludeLogic.apply(makeIncludeExcludeNode(this, context, fromClass, toClass));
	}

	static IncludeExcludeNode makeIncludeExcludeNode(final AccessRule rule, final EvaluationContext context,
			final ClassInfo fromClass, final ClassInfo toClass) {
		if (rule == null) {
			return null;
		}
		
		return new IncludeExcludeNode() {
			public boolean isInclude() {
				return rule.getType() == AccessRuleType.ALLOW;
			}

			public boolean matches() throws RulesException {
				return rule.getFrom().matches(context, fromClass) && rule.getTo().matches(context, toClass);
			}

			public IncludeExcludeNode getChild() {
				return makeIncludeExcludeNode(rule.getChild(), context, fromClass, toClass);
			}

			public IncludeExcludeNode getNext() {
				return makeIncludeExcludeNode(rule.getNext(), context, fromClass, toClass);
			}
		};
	}
}
