/*______________________________________________________________________________
 *
 * Current distribution and futher info:  http://innig.net/macker/
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
 
package net.innig.macker.event;

import net.innig.macker.rule.RuleSet;

import java.util.*;

public class ThrowingListener
    implements MackerEventListener
    {
    public ThrowingListener(boolean throwOnFirst)
        { this.throwOnFirst = throwOnFirst; }
    
    public void mackerStarted(RuleSet ruleSet)
        {
        if(ruleSet.getParent() == null)
            {
            if(events != null)
                throw new IllegalStateException("This ThrowingListener is already in use");
            events = new LinkedList();
            }
        }
    
    public void mackerFinished(RuleSet ruleSet)
        throws MackerIsMadException
        {
        if(ruleSet.getParent() == null && !events.isEmpty())
            throw new MackerIsMadException(events);
        }

    public void mackerAborted(RuleSet ruleSet)
        { events = null; }
    
    public void handleMackerIsMadEvent(RuleSet ruleSet, MackerIsMadEvent event)
        throws MackerIsMadException
        {
        if(throwOnFirst)
            throw new MackerIsMadException(event);
        events.add(event);
        }
    
    private List events;
    private boolean throwOnFirst;
    }