package net.innig.macker.rule;

import java.util.*;

public class ForEach
    extends Rule
    {
    public String getName()
        { return name; }
    
    public void setName(String name)
        { this.name = name; }
    
    public String getValue()
        { return value; }
    
    public void setValue(String value)
        { this.value = value; }
    
    public RuleSet getRuleSet()
        { return ruleSet; }
    
    public void setRuleSet(RuleSet ruleSet)
         { this.ruleSet = ruleSet; }
    
    private RuleSet ruleSet;
    private String name, value;
    }