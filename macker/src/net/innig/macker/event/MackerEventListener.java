package net.innig.macker.event;

import net.innig.macker.rule.RuleSet;

import java.util.*;

public interface MackerEventListener
    extends EventListener
    {
    /** Called before rule checking begins for the given top-level ruleset.
     *  This method is not called for child rulesets (e.g. ForEach); ruleSet.parent()
     *  will always be null.
     */
    public void mackerStarted(RuleSet ruleSet);
    
    /** Called after rule checking has finished for the given ruleset.
     */
    public void mackerFinished(RuleSet ruleSet)
        throws MackerIsMadException;
    
    /** Called after an exception has aborted rule checking for the given ruleset.
     *  <b>??</b>: Is mackerAborted called if mackerFinished() was already called, but
     *	another subsequently aborted?
     */
    public void mackerAborted(RuleSet ruleSet);
    
    /** Handles Macker's irrational anger.
     */
    public void handleMackerIsMadEvent(RuleSet ruleSet, MackerIsMadEvent event)
        throws MackerIsMadException;
    }