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

import java.io.PrintStream;
import java.io.PrintWriter;
import org.jdom.Element;
import net.innig.collect.CollectionDiff;

public abstract class EventRecording
    {
    public EventRecording(EventRecording parent)
        { this.parent = parent; }
    
    protected EventRecording getParent()
        { return parent; }
    
    public abstract EventRecording record(MackerEvent event);
    
    public abstract void read(Element elem);
    
    public boolean compare(EventRecording actual, PrintWriter out)
        {
        if(getClass() != actual.getClass())
            {
            out.println("expected " + this + ", but got " + actual);
            return false;
            }
        
        return true;
        }
    
    public void dump(PrintStream out, int indent)
        {
        PrintWriter outWriter = new PrintWriter(out);
        dump(outWriter, indent);
        outWriter.flush();
        }
        
    public void dump(PrintWriter out, int indent)
        {
        for(int n = 0; n < indent; n++)
            out.print(' ');
        out.println(this);
        }
        
    private EventRecording parent;
    }
