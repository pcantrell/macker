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

import java.io.Writer;
import java.io.PrintWriter;
import java.io.OutputStream;

public class PrintingListener
    implements MackerEventListener
    {
    public PrintingListener(PrintWriter out)
        { this.out = out; }
    
    public PrintingListener(Writer out)
        { this.out = new PrintWriter(out, true); }
    
    public PrintingListener(OutputStream out)
        { this.out = new PrintWriter(out, true); }
    
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
    
    public void handleMackerIsMadEvent(RuleSet ruleSet, MackerIsMadEvent event)
        throws MackerIsMadException
        {
        if(first)
            {
            out.println();
            first = false;
            }
        out.println(event.toStringVerbose());
        }
    
    private boolean first;
    private PrintWriter out;
    }