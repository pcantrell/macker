/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002 Paul Cantrell
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 2.1, as published
 * by the Free Software Foundation. See the file LICENSE.html for more info.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the license for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.
 * 59 Temple Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *______________________________________________________________________________
 */
 
package net.innig.macker.rule;

import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.structure.ClassManager;

import java.util.*;

public class RuleSet
    extends Rule
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
    
    public String getName()
        {
        if(name == null)
            return (parent != null) ? getParent().getName() : "<anonymous ruleset>";
        return name;
        }
    
    public void setName(String name)
        { this.name = name; }
    
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
    
    public String toString()
        { return getClass().getName() + '[' + name + ", parent=" + getParent() + ']'; }

    public void check(
            EvaluationContext parentContext,
            ClassManager classes)
        throws RulesException, MackerIsMadException
        {
        EvaluationContext context = new EvaluationContext(this, parentContext);
        context.broadcastStarted();
        boolean finished = false;
        try
            {
            for(Iterator ruleIter = rules.iterator(); ruleIter.hasNext(); )
                {
                Rule rule = (Rule) ruleIter.next();
                rule.check(context, classes);
                }
            context.broadcastFinished();
            finished = true;
            }
        finally
            {
            if(!finished)
                context.broadcastAborted();
            }
        }
    
    private String name;
    private Map/*<String,Pattern>*/ patterns;
    private Collection rules;
    private RuleSet parent;
    }


