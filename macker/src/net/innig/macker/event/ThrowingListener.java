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

import java.util.*;

public class ThrowingListener
    implements MackerEventListener
    {
    public ThrowingListener(boolean throwOnFirst, boolean throwOnFinish)
        {
        this.throwOnFirst = throwOnFirst;
        this.throwOnFinish = throwOnFinish;
        clear();
        }
    
    public void mackerStarted(RuleSet ruleSet)
        {
        if(ruleSet.getParent() == null)
            {
            if(inUse)
                throw new IllegalStateException("This ThrowingListener is already in use");
            inUse = true;
            }
        }
    
    public void mackerFinished(RuleSet ruleSet)
        throws MackerIsMadException
        {
        if(ruleSet.getParent() == null)
            {
            inUse = false;
            if(throwOnFinish)
                timeToGetMad();
            }
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

    public void timeToGetMad()
        throws MackerIsMadException
        {
        if(!events.isEmpty())
            throw new MackerIsMadException(events);
        }
    
    public void clear()
        { events = new LinkedList(); }
    
    private boolean throwOnFirst, throwOnFinish;
    private List events;
    private boolean inUse;
    }