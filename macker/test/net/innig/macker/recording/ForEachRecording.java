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

import net.innig.macker.event.*;
import net.innig.macker.rule.Rule;
import net.innig.macker.rule.RuleSet;

import java.io.PrintWriter;
import java.util.*;

import org.jdom.Element;

import net.innig.collect.CollectionDiff;

public class ForEachRecording
    extends EventRecording
    {
    public ForEachRecording(EventRecording parent)
        {
        super(parent);
        iterations = new TreeMap();
        }
    
    public EventRecording record(MackerEvent event)
        {
        if(!(event instanceof ForEachEvent))
            return getParent().record(event);
        
        if(event instanceof ForEachStarted)
            var = ((ForEachEvent) event).getForEach().getVariableName();
        
        if(event instanceof ForEachIterationStarted)
            {
            String value = ((ForEachIterationStarted) event).getVariableValue();
            RuleSetRecording ruleSetRec = new RuleSetRecording(this);
            iterations.put(value, ruleSetRec);
            return ruleSetRec;
            }
        
        if(event instanceof ForEachFinished)
            return getParent();
        
        return this;
        }
        
    public void read(Element elem)
        {
        var = elem.getAttributeValue("var");
        for(Iterator childIter = elem.getChildren("iteration").iterator(); childIter.hasNext(); )
            {
            Element iterElem = (Element) childIter.next();
            
            String varValue = iterElem.getAttributeValue("value");
            EventRecording iter = new RuleSetRecording(this);
            iter.read(iterElem);
            iterations.put(varValue, iter);
            }
        }
    
    public boolean compare(EventRecording actual, PrintWriter out)
        {
        if(!super.compare(actual, out))
            return false;
        
        boolean match = true;
        
        ForEachRecording actualForEach = (ForEachRecording) actual;
        if(!var.equals(actualForEach.var))
            {
            out.println("Expected " + this + ", but got " + actual);
            match = false;
            }
        
        CollectionDiff diff = new CollectionDiff(iterations.keySet(), actualForEach.iterations.keySet());
        if(!diff.getRemoved().isEmpty())
            out.println(this + ": missing iterations: " + diff.getRemoved());
        if(!diff.getAdded().isEmpty())
            out.println(this + ": unexpected iterations: " + diff.getAdded());
        match = match && diff.getRemoved().isEmpty() && diff.getAdded().isEmpty();
        
        for(Iterator i = diff.getSame().iterator(); i.hasNext(); )
            {
            String varValue = (String) i.next();
//            out.println("(comparing " + var + "=" + varValue + ")");
            RuleSetRecording iterExpected = (RuleSetRecording) iterations.get(varValue);
            RuleSetRecording iterActual   = (RuleSetRecording) actualForEach.iterations.get(varValue);
            match = iterExpected.compare(iterActual, out) && match;
            }
        return match;
        }
    
    public String toString()
        { return "[foreach:" + var + "]"; }
    
    public void dump(PrintWriter out, int indent)
        {
        super.dump(out, indent);
        for(Iterator entryIter = iterations.entrySet().iterator(); entryIter.hasNext(); )
            {
            Map.Entry entry = (Map.Entry) entryIter.next();
            for(int n = -3; n < indent; n++)
                out.print(' ');
            out.println("[iteration:" + entry.getKey() + "]");
            ((EventRecording) entry.getValue()).dump(out, indent+6);
            }
        }
    
    private String var;
    private Map/*<String,RuleSetRecording>*/ iterations;
    }



