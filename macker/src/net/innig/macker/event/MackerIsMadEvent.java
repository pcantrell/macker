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

import net.innig.macker.rule.Rule;

import java.util.*;

public class MackerIsMadEvent
    extends EventObject
    {
    public MackerIsMadEvent(Rule rule, String description, List messages /*,severity?*/ )
        {
        super(rule);
        this.rule = rule;
        this.description = description;
        this.messages = Collections.unmodifiableList(new ArrayList(messages));
        }
    
    public Rule getRule()
        { return rule; }
        
    public String getDescription()
        { return description; }
        
    public List getMessages()
        { return messages; }
    
    public String toString()
        { return getDescription(); }
    
    public String toStringVerbose()
        {
        final String CR = System.getProperty("line.separator");
        StringBuffer s = new StringBuffer();
        for(Iterator i = messages.iterator(); i.hasNext(); )
            {
            s.append(i.next().toString());
            s.append(CR);
            }
        s.append(getDescription());
        s.append(CR);
        return s.toString();
        }
    
    private final Rule rule;
    private final String description;
    private final List messages;
    }