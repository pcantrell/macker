package net.innig.macker.rule;

import org.jdom.Element;

/**
    Indicates a structural exception in rules specification XML.
*/

public class RulesDocumentException
    extends RulesException
    {
    public RulesDocumentException(Element element, String message)
        {
        super("Error in rules document: " + message + " (Offending element: " + element + ')');
        this.element = element;
        }
    
    public final Element getElement()
        { return element; }
    
    private final Element element;
    }