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
        if(getParent().getRuleSet() != getRuleSet().getParent())
            throw new IllegalArgumentException(
                "Parent EvaluationContext must be associated with parent RuleSet");
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
    
    public void addListener(MackerEventListener listener)
        { listeners.add(listener); }
        
    public void removeListener(MackerEventListener listener)
        { listeners.remove(listener); }
    
    public void broadcastStarted()
        {
        for(Iterator i = listeners.iterator(); i.hasNext(); )
            ((MackerEventListener) i.next()).mackerStarted(ruleSet);
        if(getParent() != null)
            getParent().broadcastStarted();
        }
    
    public void broadcastFinished()
        throws MackerIsMadException
        {
        for(Iterator i = listeners.iterator(); i.hasNext(); )
            ((MackerEventListener) i.next()).mackerFinished(ruleSet);
        if(getParent() != null)
            getParent().broadcastFinished();
        }
    
    public void broadcastAborted()
        {
        for(Iterator i = listeners.iterator(); i.hasNext(); )
            ((MackerEventListener) i.next()).mackerAborted(ruleSet);
        if(getParent() != null)
            getParent().broadcastAborted();
        }
    
    public void broadcastEvent(MackerIsMadEvent event)
        throws MackerIsMadException
        {
        for(Iterator i = listeners.iterator(); i.hasNext(); )
            ((MackerEventListener) i.next()).handleMackerIsMadEvent(ruleSet, event);
        if(getParent() != null)
            getParent().broadcastEvent(event);
        }
        
    private RuleSet ruleSet;
    private EvaluationContext parent;
    private Map varValues;
    private Set listeners;
    }



