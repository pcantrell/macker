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
 
package net.innig.macker.recording;

import net.innig.macker.event.ForEachIterationFinished;
import net.innig.macker.event.ForEachStarted;
import net.innig.macker.event.MackerEvent;
import net.innig.macker.rule.RuleSet;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

public class RuleSetRecording
    extends EventRecording
    {
    public RuleSetRecording(EventRecording parent)
        {
        super(parent);
        children = new ArrayList<EventRecording>();
        }
    
    public EventRecording record(MackerEvent event)
        {
        if(name == null)
            {
            RuleSet ruleSet = event.getRule().getParent();
            name = ruleSet.hasName() ? ruleSet.getName() : null;
            }
        
        EventRecording child;
        if(event instanceof ForEachStarted)
            child = new ForEachRecording(this);
        else if(event instanceof ForEachIterationFinished)
            return getParent().record(event);
        else
            child = new GenericRuleRecording(this);
        children.add(child);
        return child.record(event);
        }
    
    public void read(Element elem)
        {
        for(Element childElem : (List<Element>) elem.getChildren())
            {
            EventRecording child;
            if(childElem.getName().equals("rule"))
                child = new GenericRuleRecording(this);
            else if(childElem.getName().equals("foreach"))
                child = new ForEachRecording(this);
            else
                throw new RuntimeException("Unknown element: " + childElem);
            child.read(childElem);
            children.add(child);
            }
        }
    
    public boolean compare(EventRecording actual, PrintWriter out)
        {
        if(!super.compare(actual, out))
            return false;
        
        RuleSetRecording actualRuleSet = (RuleSetRecording) actual;
        
        if(children.size() != actualRuleSet.children.size())
            {
            out.println(
                "expected " + children.size()
                + " rules generating events, but got " + actualRuleSet.children.size()
                + ": " + actualRuleSet.children);
            return false;
            }
        
        boolean match = true;
        Iterator<EventRecording>
            expectedIter = children.iterator(),
            actualIter = actualRuleSet.children.iterator();
        while(expectedIter.hasNext())
            {
            EventRecording expectedChild = expectedIter.next();
            EventRecording   actualChild =   actualIter.next();
            match = expectedChild.compare(actualChild, out) && match;
            }
        return match;
        }
    
    public String toString()
        { return (name == null) ? "[ruleset]" : "[ruleset:" + name + "]"; }
    
    public void dump(PrintWriter out, int indent)
        {
        super.dump(out, indent);
        for(EventRecording child : children)
            child.dump(out, indent+3);
        }
    
    private String name;
    private List<EventRecording> children;
    }




