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
import net.innig.macker.event.MackerEvent;
import net.innig.macker.event.MackerEventListener;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.structure.ClassManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Paul Cantrell
 */
public class EvaluationContext {
	
	private RuleSet ruleSet;
	private EvaluationContext parent;
	private Map<String, String> varValues;
	private Set<MackerEventListener> listeners;
	private ClassManager classManager;
	
	public EvaluationContext(final ClassManager classManager, final RuleSet ruleSet) {
		this.classManager = classManager;
		this.ruleSet = ruleSet;
		this.varValues = new HashMap<String, String>();
		this.listeners = new HashSet<MackerEventListener>();
	}

	public EvaluationContext(final RuleSet ruleSet, final EvaluationContext parent) {
		this(parent.getClassManager(), ruleSet);
		this.parent = parent;
	}

	public EvaluationContext(final EvaluationContext parent) {
		this(parent.getRuleSet(), parent);
	}

	public EvaluationContext getParent() {
		return this.parent;
	}

	public ClassManager getClassManager() {
		return this.classManager;
	}

	public RuleSet getRuleSet() {
		return this.ruleSet;
	}

	public void setVariableValue(final String name, final String value) throws UndeclaredVariableException {
		String putValue = "";
		if (value != null) {
			putValue = VariableParser.parse(this, value);
		}
		getVarValues().put(name, putValue);
	}

	public String getVariableValue(final String name) throws UndeclaredVariableException {
		final String value = getVarValues().get(name);
		if (value != null) {
			return value;
		}
		if (getParent() != null) {
			return getParent().getVariableValue(name);
		}
		throw new UndeclaredVariableException(name);
	}

	public void setVariables(final Map<String, String> vars) {
		getVarValues().putAll(vars);
	}

	public void addListener(final MackerEventListener listener) {
		getListeners().add(listener);
	}

	public void removeListener(final MackerEventListener listener) {
		getListeners().remove(listener);
	}

	public void broadcastStarted() throws ListenerException {
		broadcastStarted(getRuleSet());
	}

	protected void broadcastStarted(final RuleSet targetRuleSet) throws ListenerException {
		for (MackerEventListener listener : getListeners()) {
			listener.mackerStarted(targetRuleSet);
		}
		if (getParent() != null) {
			getParent().broadcastStarted(targetRuleSet);
		}
	}

	public void broadcastFinished() throws MackerIsMadException, ListenerException {
		broadcastFinished(getRuleSet());
	}

	protected void broadcastFinished(final RuleSet targetRuleSet) throws MackerIsMadException, ListenerException {
		for (MackerEventListener listener : getListeners()) {
			listener.mackerFinished(targetRuleSet);
		}
		if (getParent() != null) {
			getParent().broadcastFinished(targetRuleSet);
		}
	}

	public void broadcastAborted() {
		broadcastAborted(getRuleSet());
	}

	protected void broadcastAborted(final RuleSet targetRuleSet) {
		for (MackerEventListener listener : getListeners()) {
			listener.mackerAborted(targetRuleSet);
		}
		if (getParent() != null) {
			getParent().broadcastAborted(targetRuleSet);
		}
	}

	public void broadcastEvent(final MackerEvent event) throws MackerIsMadException, ListenerException {
		broadcastEvent(event, getRuleSet());
	}

	protected void broadcastEvent(final MackerEvent event, final RuleSet targetRuleSet) throws MackerIsMadException,
			ListenerException {
		for (MackerEventListener listener : getListeners()) {
			listener.handleMackerEvent(targetRuleSet, event);
		}
		if (getParent() != null) {
			getParent().broadcastEvent(event, targetRuleSet);
		}
	}
	
	private Set<MackerEventListener> getListeners() {
		return this.listeners;
	}
	
	private Map<String, String> getVarValues() {
		return this.varValues;
	}
}
