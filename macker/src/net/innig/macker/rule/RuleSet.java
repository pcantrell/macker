package net.innig.macker.rule;

import net.innig.macker.MackerIsMadException;

import java.util.*;

public class RuleSet
    {
    public RuleSet()
        {
        patterns = new HashMap();
        rules = new ArrayList();
        }

    public RuleSet(RuleSet parent)
        {
        this();
        setParent(parent);
        }
    
    public boolean declaresPattern(String name)
        { return patterns.keySet().contains(name); }
    
    public Pattern getPattern(String name)
        {
        Pattern pat = (Pattern) patterns.get(name);
        if(pat != null)
            return pat;
        if(parent != null)
            return parent.getPattern(name);
        return null;
        }
    
    public void setPattern(String name, Pattern pattern)
        {
        if(name == null)
            throw new NullPointerException("name cannot be null");
        if(pattern == null)
            throw new NullPointerException("pattern cannot be null");
        patterns.put(name, pattern);
        }
    
    public Collection getAllPatterns()
        { return patterns.values(); }
    
    public void clearPattern(String name)
        { patterns.remove(name); }
    
    public Collection getRules()
        { return rules; }
    
    public void addRule(Rule rule)
        { rules.add(rule); }
    
    public RuleSet getParent()
        { return parent; }
    
    public void setParent(RuleSet parent)
        { this.parent = parent; }
    
    private Map/*<String,Pattern>*/ patterns;
    private Collection rules;
    private RuleSet parent;
    }


