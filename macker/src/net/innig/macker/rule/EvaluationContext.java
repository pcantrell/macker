/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002 Paul Cantrell
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

import net.innig.macker.event.MackerIsMadEvent;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.event.MackerEventListener;

import java.util.*;

public class EvaluationContext
    {
    public EvaluationContext(RuleSet ruleSet)
        {
        this.ruleSet = ruleSet;
        varValues = new HashMap();
        listeners = new HashSet();
        }
    
    public EvaluationContext(RuleSet ruleSet, EvaluationContext parent)
        {
        this(ruleSet);
        this.parent = parent;
// Relaxed to allow ForEach to have its own context; should ForEach extend RuleSet?
//        if(getParent().getRuleSet() != getRuleSet().getParent())
//            throw new IllegalArgumentException(
//                "Parent EvaluationContext must be associated with parent RuleSet");
        }
    
    public EvaluationContext getParent()
        { return parent; }
    
    public RuleSet getRuleSet()
        { return ruleSet; }
    
    public void setVariableValue(String name, String value)
        { varValues.put(name, value); }
    
    public String getVariableValue(String name)
        throws UndeclaredVariableException
        {
        String value = (String) varValues.get(name);
        if(value != null)
            return value;
        if(parent != null)
            return parent.getVariableValue(name);
        throw new UndeclaredVariableException(name);
        }
        
    public void setVariables(Map/*<String,String>*/ vars)
        { varValues.putAll(vars); }
    
    public void addListener(MackerEventListener listener)
        { listeners.add(listener); }
        
    public void removeListener(MackerEventListener listener)
        { listeners.remove(listener); }
    
    public void broadcastStarted()
        { broadcastStarted(getRuleSet()); }
        
    protected void broadcastStarted(RuleSet targetRuleSet)
        {
        for(Iterator i = listeners.iterator(); i.hasNext(); )
            ((MackerEventListener) i.next()).mackerStarted(targetRuleSet);
        if(getParent() != null)
            getParent().broadcastStarted(targetRuleSet);
        }
    
    public void broadcastFinished()
        throws MackerIsMadException
        { broadcastFinished(getRuleSet()); }
        
    protected void broadcastFinished(RuleSet targetRuleSet)
        throws MackerIsMadException
        {
        for(Iterator i = listeners.iterator(); i.hasNext(); )
            ((MackerEventListener) i.next()).mackerFinished(targetRuleSet);
        if(getParent() != null)
            getParent().broadcastFinished(targetRuleSet);
        }
    
    public void broadcastAborted()
        { broadcastAborted(getRuleSet()); }
        
    protected void broadcastAborted(RuleSet targetRuleSet)
        {
        for(Iterator i = listeners.iterator(); i.hasNext(); )
            ((MackerEventListener) i.next()).mackerAborted(targetRuleSet);
        if(getParent() != null)
            getParent().broadcastAborted(targetRuleSet);
        }
    
    public void broadcastEvent(MackerIsMadEvent event)
        throws MackerIsMadException
        { broadcastEvent(event, getRuleSet()); }
        
    protected void broadcastEvent(MackerIsMadEvent event, RuleSet targetRuleSet)
        throws MackerIsMadException
        {
        for(Iterator i = listeners.iterator(); i.hasNext(); )
            ((MackerEventListener) i.next()).handleMackerIsMadEvent(targetRuleSet, event);
        if(getParent() != null)
            getParent().broadcastEvent(event, targetRuleSet);
        }
        
    private RuleSet ruleSet;
    private EvaluationContext parent;
    private Map varValues;
    private Set listeners;
    }



