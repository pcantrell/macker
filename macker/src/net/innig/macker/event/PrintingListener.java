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
 
package net.innig.macker.event;

import net.innig.macker.rule.RuleSet;
import net.innig.macker.rule.RuleSeverity;

import java.io.Writer;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.*;
import net.innig.collect.MultiMap;
import net.innig.collect.CompositeMultiMap;

public class PrintingListener
    implements MackerEventListener
    {
    public PrintingListener(PrintWriter out)
        { this.out = out; }
    
    public PrintingListener(Writer out)
        { this.out = new PrintWriter(out, true); }
    
    public PrintingListener(OutputStream out)
        { this.out = new PrintWriter(out, true); }
    
    public void setThreshold(RuleSeverity threshold)
        { this.threshold = threshold; }
        
    public void mackerStarted(RuleSet ruleSet)
        {
        if(ruleSet.getParent() == null || ruleSet.hasName())
            {
            out.println();
            out.println("(Checking ruleset: " + ruleSet.getName() + " ...)");
            first = true;
            }
        }
    
    public void mackerFinished(RuleSet ruleSet)
        throws MackerIsMadException
        { }

    public void mackerAborted(RuleSet ruleSet)
        { } // don't care
    
    public void handleMackerEvent(RuleSet ruleSet, MackerEvent event)
        throws MackerIsMadException
        {
        eventsBySeverity.put(event.getRule().getSeverity(), event);
        if(event.getRule().getSeverity().compareTo(threshold) >= 0)
            {
            if(first)
                {
                out.println();
                first = false;
                }
            out.println(event.toStringVerbose());
            }
        }
    
    public void printSummary()
        {
        // output looks like: "(2 errors, 1 warning)"
        boolean firstSeverity = true;
        List severities = new ArrayList(eventsBySeverity.keySet());
        Collections.reverse(severities);
        for(Iterator i = severities.iterator(); i.hasNext(); )
            {
            RuleSeverity severity = (RuleSeverity) i.next();
            Collection eventsForSev = eventsBySeverity.get(severity);
            if(eventsForSev.size() > 0)
                {
                if(firstSeverity)
                    out.print("(");
                else
                    out.print(", ");
                firstSeverity = false;
                out.print(eventsForSev.size());
                out.print(' ');
                out.print((eventsForSev.size() == 1)
                    ? severity.getName()
                    : severity.getNamePlural());
                }
            }
        if(!firstSeverity)
            out.println(')');
        }
        
    private boolean first;
    private PrintWriter out;
    private RuleSeverity threshold = RuleSeverity.INFO;
    private final MultiMap eventsBySeverity = new CompositeMultiMap(TreeMap.class, HashSet.class);
    }

