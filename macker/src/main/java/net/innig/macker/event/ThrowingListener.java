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

import net.innig.macker.rule.RuleSet;
import net.innig.macker.rule.RuleSeverity;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Paul Cantrell
 *
 */
public class ThrowingListener implements MackerEventListener {
	
	private final RuleSeverity throwOnFirstThreshold;
	private final RuleSeverity throwOnFinishThreshold;
	private RuleSeverity maxSeverity;
	private List<MackerEvent> events;
	private boolean inUse;
	
	public ThrowingListener(final RuleSeverity throwOnFirstThreshold, final RuleSeverity throwOnFinishThreshold) {
		this.throwOnFirstThreshold = throwOnFirstThreshold;
		this.throwOnFinishThreshold = throwOnFinishThreshold;
		clear();
	}

	public void mackerStarted(final RuleSet ruleSet) {
		if (ruleSet.getParent() == null) {
			if (isInUse()) {
				throw new IllegalStateException("This ThrowingListener is already in use");
			}
			setInUse(true);
		}
	}

	public void mackerFinished(final RuleSet ruleSet) throws MackerIsMadException {
		if (ruleSet.getParent() == null) {
			setInUse(false);
		}
	}

	public void mackerAborted(final RuleSet ruleSet) {
		setEvents(null);
	}

	public void handleMackerEvent(final RuleSet ruleSet, final MackerEvent event) throws MackerIsMadException {
		if (event instanceof ForEachEvent) {
			return;
		}

		final RuleSeverity severity = event.getRule().getSeverity();
		if (getMaxSeverity() == null || severity.compareTo(getMaxSeverity()) >= 0) {
			setMaxSeverity(severity);
		}

		if (getThrowOnFinishThreshold() != null && severity.compareTo(getThrowOnFinishThreshold()) >= 0) {
			getEvents().add(event);
		}

		timeToGetMad(getThrowOnFirstThreshold());
	}

	public void timeToGetMad(final RuleSeverity threshold) throws MackerIsMadException {
		if (threshold != null && getMaxSeverity() != null && getMaxSeverity().compareTo(threshold) >= 0) {
			timeToGetMad();
		}
	}

	public void timeToGetMad() throws MackerIsMadException {
		if (!getEvents().isEmpty()) {
			throw new MackerIsMadException(getEvents());
		}
	}

	public void clear() {
		setEvents(new LinkedList<MackerEvent>());
	}

	public String toString() {
		return "ThrowingListener";
	}
	
	private List<MackerEvent> getEvents() {
		return this.events;
	}
	
	private void setEvents(final List<MackerEvent> events) {
		this.events = events;
	}
	
	private boolean isInUse() {
		return this.inUse;
	}
	
	private void setInUse(final boolean inUse) {
		this.inUse = inUse;
	}
	
	private RuleSeverity getMaxSeverity() {
		return this.maxSeverity;
	}
	
	private void setMaxSeverity(final RuleSeverity maxSeverity) {
		this.maxSeverity = maxSeverity;
	}
	
	private RuleSeverity getThrowOnFinishThreshold() {
		return this.throwOnFinishThreshold;
	}
	
	private RuleSeverity getThrowOnFirstThreshold() {
		return this.throwOnFirstThreshold;
	}
}
