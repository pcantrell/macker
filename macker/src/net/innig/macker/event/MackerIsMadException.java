package net.innig.macker.event;

import net.innig.macker.rule.Rule;

import java.util.*;

public class MackerIsMadException
    extends Exception
    {
    public MackerIsMadException(MackerIsMadEvent event)
        {
        super(BASE_MESSAGE + event);
        this.events = Collections.singletonList(event);
        }

    public MackerIsMadException(List/*<MackerIsMadEvent>*/ events)
        {
        super(BASE_MESSAGE + events);
        if(events.isEmpty())
            throw new IllegalArgumentException("Macker needs a non-empty list of things to be mad about.");
        this.events = Collections.unmodifiableList(new ArrayList(events));
        }
    
    public List getEvents()
         { return events; }
    
    private final List events;
    
    private static final String BASE_MESSAGE = "Macker rules checking failed: ";
    }