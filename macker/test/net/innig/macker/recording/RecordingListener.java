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

import net.innig.macker.event.MackerEvent;
import net.innig.macker.event.MackerEventListener;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.rule.RuleSet;

public class RecordingListener
    implements MackerEventListener
    {
    public RecordingListener()
        { recording = curRecording = new RuleSetRecording(null); }
    
    public void mackerStarted(RuleSet ruleSet)
        { }
    
    public void mackerFinished(RuleSet ruleSet)
        throws MackerIsMadException
        { }

    public void mackerAborted(RuleSet ruleSet)
        { } // don't care
    
    public void handleMackerEvent(RuleSet ruleSet, MackerEvent event)
        throws MackerIsMadException
        { curRecording = curRecording.record(event); }
    
    public EventRecording getRecording()
        { return recording; }
    
    public String toString()
        { return "RecordingListener"; }
    
    private EventRecording recording;
    private EventRecording curRecording;
    }

