package net.innig.macker.event;

import net.innig.macker.rule.RuleSet;

import java.util.*;

public class ThrowingListener
    implements MackerEventListener
    {
    public ThrowingListener(boolean throwOnFirst)
        { this.throwOnFirst = throwOnFirst; }
    
    public void mackerStarted(RuleSet ruleSet)
        {
        if(events != null)
            throw new IllegalStateException("This ThrowListener has already been used");
        events = new LinkedList();
        }
    
    public void mackerFinished(RuleSet ruleSet)
        throws MackerIsMadException
        {
        if(!events.isEmpty())
            throw new MackerIsMadException(events);
        }

    public void mackerAborted(RuleSet ruleSet)
        { } // don't care
    
    public void handleMackerIsMadEvent(RuleSet ruleSet, MackerIsMadEvent event)
        throws MackerIsMadException
        {
        if(throwOnFirst)
            throw new MackerIsMadException(event);
        events.add(event);
        }
    
    private List events;
    private boolean throwOnFirst;
    }