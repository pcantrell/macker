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

import java.util.*;

public class ThrowingListener
    implements MackerEventListener
    {
    public ThrowingListener()
        { this(null, null); }
    
    public ThrowingListener(
            RuleSeverity throwOnFirstThreshold,
            RuleSeverity throwOnFinishThreshold)
        {
        this.throwOnFirstThreshold = throwOnFirstThreshold;
        this.throwOnFinishThreshold = throwOnFinishThreshold;
        clear();
        }
    
    public void mackerStarted(RuleSet ruleSet)
        {
        if(ruleSet.getParent() == null)
            {
            if(inUse)
                throw new IllegalStateException("This ThrowingListener is already in use");
            inUse = true;
            }
        }
    
    public void mackerFinished(RuleSet ruleSet)
        throws MackerIsMadException
        {
        if(ruleSet.getParent() == null)
            {
            inUse = false;
            timeToGetMad(throwOnFinishThreshold);
            }
        }

    public void mackerAborted(RuleSet ruleSet)
        { events = null; }
    
    public void handleMackerEvent(RuleSet ruleSet, MackerEvent event)
        throws MackerIsMadException
        {
        if(event instanceof ForEachEvent)
            return;
        
        events.add(event);

        RuleSeverity severity = event.getRule().getSeverity();
        if(maxSeverity == null || severity.compareTo(maxSeverity) >= 0)
            maxSeverity = severity;

        timeToGetMad(throwOnFirstThreshold);
        }
    
    public void timeToGetMad(RuleSeverity threshold)
        throws MackerIsMadException
        {
        if(threshold != null && maxSeverity != null && maxSeverity.compareTo(threshold) >= 0)
            timeToGetMad();
        }

    public void timeToGetMad()
        throws MackerIsMadException
        {
        if(!events.isEmpty())
            throw new MackerIsMadException(events);
        }
    
    public void clear()
        { events = new LinkedList(); }
    
    public String toString()
        { return "ThrowingListener"; }
    
    private final RuleSeverity throwOnFirstThreshold, throwOnFinishThreshold;
    private RuleSeverity maxSeverity;
    private List events;
    private boolean inUse;
    }
