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

import net.innig.collect.CollectionDiff;
import net.innig.macker.event.AccessRuleViolation;
import net.innig.macker.event.MackerEvent;
import net.innig.macker.event.MessageEvent;
import net.innig.macker.rule.Rule;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jdom.Attribute;
import org.jdom.Element;

public class GenericRuleRecording
    extends EventRecording
    {
    public GenericRuleRecording(EventRecording parent)
        {
        super(parent);
        events = new HashSet<Map<String,String>>();
        }
    
    public EventRecording record(MackerEvent event)
        {
        if(rule == null)
            rule = event.getRule();
        if(event.getRule() != rule)
            return getParent().record(event);
        
        Map<String,String> eventAttributes = new TreeMap<String,String>();
        eventType = event.getClass().getName();
        if(eventType.startsWith(DEFAULT_EVENT_PACKAGE))
            eventType = eventType.substring(DEFAULT_EVENT_PACKAGE.length());
        eventAttributes.put("type", eventType);
        eventAttributes.put("severity", event.getRule().getSeverity().getName());
        int msgNum = 0;
        for(String msg : event.getMessages())
            eventAttributes.put("message" + msgNum, msg);

        if(event instanceof MessageEvent)
            { } // done already!
        else if(event instanceof AccessRuleViolation)
            {
            AccessRuleViolation arv = (AccessRuleViolation) event;
            eventAttributes.put("from", arv.getFrom().getFullName());
            eventAttributes.put("to", arv.getTo().getFullName());
            }
        else
            throw new IllegalArgumentException("Unknown event type: " + event);
        
        events.add(eventAttributes);
        
        return this;
        }
    
    public void read(Element elem)
        {
        Map baseAtt = getAttributeValueMap(elem);
        for(Element eventElem : (List<Element>) elem.getChildren("event"))
            {
            Map<String,String> eventAtt = new TreeMap<String,String>(baseAtt);
            eventAtt.putAll(getAttributeValueMap(eventElem));
            eventType = eventAtt.get("type");
            events.add(eventAtt);
            }
        }
    
    private Map<String,String> getAttributeValueMap(Element elem)
        {
        Map<String,String> attValues = new TreeMap<String,String>();
        for(Attribute attr : (List<Attribute>) elem.getAttributes())
            attValues.put(attr.getName(), attr.getValue());
        return attValues;
        }
    
    public boolean compare(EventRecording actual, PrintWriter out)
        {
        if(!super.compare(actual, out))
            return false;

        boolean match = true;
        GenericRuleRecording actualGRR = (GenericRuleRecording) actual;
        Set expectedSet = events;
        Set actualSet = actualGRR.events;
        CollectionDiff diff = new CollectionDiff(expectedSet, actualSet);
        if(!diff.getRemoved().isEmpty())
            {
            out.println(this + ": missing events:");
            dump(out, diff.getRemoved());
            match = false;
            }
        if(!diff.getAdded().isEmpty())
            {
            out.println(this + ": unexpected events:");
            dump(out, diff.getAdded());
            match = false;
            }
        return match;
        }
    
    private void dump(PrintWriter out, Collection events)
        {
        for(Object event : events)
            out.println("    " + event);
        }
    
    public String toString()
        { return "[rule:" + eventType + "]"; }
    
    public void dump(PrintWriter out, int indent)
        {
        super.dump(out, indent);
        for(Map event : events)
            {
            for(int n = -3; n < indent; n++)
                out.print(' ');
            out.println(event);
            }
        }
    
    private Rule rule;
    private String eventType;
    private Set<Map<String,String>> events;
    private static final String DEFAULT_EVENT_PACKAGE = "net.innig.macker.event.";
    }


