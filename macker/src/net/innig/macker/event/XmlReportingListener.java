package net.innig.macker.event;

import net.innig.macker.rule.RuleSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

public class XmlReportingListener
    implements MackerEventListener
    {
 	private Writer out;
    private String encoding;
    
    private Document document;
    private Element reportElem;
    private Element curElem;

    public XmlReportingListener(File outFile)
        throws ListenerException
        {
        try {
            if(outFile.exists())
                outFile.delete();
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outFile));
            BufferedWriter bufferedOut = new BufferedWriter(out);
            init(bufferedOut, out.getEncoding());
            }
        catch(IOException ioe)
            { throw new ListenerException(this, "Unable to remove and re-create report file \"" + outFile + "\"", ioe); }
        }
        
    public XmlReportingListener(Writer out, String encoding)
        throws ListenerException
        { init(out, encoding); }
    
    private void init(Writer out, String encoding)
        throws ListenerException
        {
        this.out = out;
        this.encoding = encoding;
        
        curElem = reportElem = new Element("macker-report");
        document = new Document(reportElem);
        }
        
    public void flush()
        throws ListenerException
        {
        try
            {
            XMLOutputter xmlOut = new XMLOutputter("    ", true, encoding);
            xmlOut.output(document, out);
            out.flush();
            }
        catch(IOException ioe)
            { throw new ListenerException(this, "Unable to write XML report", ioe); }
        }
    
    public void close()
        throws ListenerException
        {
        try { out.close(); }
        catch(IOException ioe)
            { throw new ListenerException(this, "Unable to close XML report", ioe); }
        }

	public void mackerStarted(RuleSet ruleSet)
        {
        Element ruleSetElem = new Element("ruleset");
        if(ruleSet.hasName())
            ruleSetElem.setAttribute("name", ruleSet.getName());
        
        curElem.addContent(ruleSetElem);
        curElem = ruleSetElem;
        }

	public void mackerFinished(RuleSet ruleSet)
        throws MackerIsMadException, ListenerException
        { curElem = curElem.getParent(); }

	public void mackerAborted(RuleSet ruleSet)
        { curElem = null; }

	public void handleMackerEvent(RuleSet ruleSet, MackerEvent event)
		throws MackerIsMadException
        {
        if(event instanceof MessageEvent)
            addMessageElements(curElem, event.getMessages());
        
        if(event instanceof AccessRuleViolation)
            {
            AccessRuleViolation violation = (AccessRuleViolation) event;
            Element violationElem = new Element("access-rule-violation");
            
            addMessageElements(violationElem, violation.getMessages());
            
            Element fromElem = new Element("from");
            fromElem.setText(violation.getFrom().getClassName());
            violationElem.addContent(fromElem);
            
            Element toElem = new Element("to");
            toElem.setText(violation.getTo().getClassName());
            violationElem.addContent(toElem);
            
            curElem.addContent(violationElem);
            }
        }

    private void addMessageElements(Element elem, List/*<String>*/ messages)
        {
        for(Iterator msgIter = messages.iterator(); msgIter.hasNext(); )
            {
            String message = (String) msgIter.next();
            Element messageElem = new Element("message");
            messageElem.setText(message);
            elem.addContent(messageElem);
            }
        }
    
    public String toString()
        { return "XmlReportingListener"; }
    }
