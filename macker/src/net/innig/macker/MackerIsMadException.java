package net.innig.macker;

import net.innig.macker.rule.Rule;

import java.util.*;

public class MackerIsMadException
    extends Exception
    {
    public MackerIsMadException(Rule rule, String from, String to)
        {
        super("Illegal reference from " + from + " to " + to);
        ruleMessages = new ArrayList();
        }
    
    public Rule getRule()
        { return rule; }
    
    public String getFrom()
        { return from; }
        
    public String getTo()
        { return to; }
    
    public List getRuleMessages()
        { return Collections.unmodifiableList(ruleMessages); }
    
    public void addRuleMessage(String message)
        { ruleMessages.add(message); }
    
    private Rule rule;
    private String from, to;
    private List ruleMessages;
    }