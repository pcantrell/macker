/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2003 Paul Cantrell
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

import net.innig.macker.event.*;
import net.innig.macker.rule.Rule;
import net.innig.macker.rule.RuleSet;

import java.util.*;

public class RecordingListener
    implements MackerEventListener
    {
    public RecordingListener()
        { recordedEvents = new ArrayList(); }
    
    public void mackerStarted(RuleSet ruleSet)
        { }
    
    public void mackerFinished(RuleSet ruleSet)
        throws MackerIsMadException
        { }

    public void mackerAborted(RuleSet ruleSet)
        { } // don't care
    
    public void handleMackerEvent(RuleSet ruleSet, MackerEvent event)
        throws MackerIsMadException
        {
        if(curRule != event.getRule())
            {
            curRule = event.getRule();
            curRuleEvents = new HashSet();
            recordedEvents.add(curRuleEvents);
            }
            
        Map eventAttributes = new TreeMap();
        String eventType = event.getClass().getName();
        if(eventType.startsWith(DEFAULT_EVENT_PACKAGE))
            eventType = eventType.substring(DEFAULT_EVENT_PACKAGE.length());
        eventAttributes.put("type", eventType);
        eventAttributes.put("severity", event.getRule().getSeverity().getName());
        int msgNum = 0;
        for(Iterator msgIter = event.getMessages().iterator(); msgIter.hasNext(); msgNum++)
            eventAttributes.put("message" + msgNum, msgIter.next());
        if(event instanceof MessageEvent)
            { } // done already!
        else if(event instanceof AccessRuleViolation)
            {
            AccessRuleViolation arv = (AccessRuleViolation) event;
            eventAttributes.put("from", arv.getFrom().getFullName());
            eventAttributes.put("to", arv.getTo().getFullName());
            }
        else if(event instanceof ForEachEvent)
            {
            eventAttributes.put("var", ((ForEachEvent) event).getForEach().getVariableName());
            if(event instanceof ForEachIterationStarted)
                eventAttributes.put("value", ((ForEachIterationStarted) event).getVariableValue());
            else if(event instanceof ForEachIterationFinished)
                eventAttributes.put("value", ((ForEachIterationFinished) event).getVariableValue());
            }
        else
            throw new IllegalArgumentException("Unknown event type: " + event);
        
        curRuleEvents.add(eventAttributes);
        }
    
    public List getRecordedEvents()
        { return recordedEvents; }
    
    public String toString()
        { return "RecordingListener"; }
    
    /**
     * List of rules
     *   Set of events for each rule
     *     Map of attributes for each event
     **/
    private List/*<Set<Map<String,String>>>*/ recordedEvents;
    private Rule curRule;
    private Set/*<Map<String,String>>*/ curRuleEvents;
    private static final String DEFAULT_EVENT_PACKAGE = "net.innig.macker.event.";
    }

