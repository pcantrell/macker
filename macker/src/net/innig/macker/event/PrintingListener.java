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
        if(ruleSet.getParent() == null)
            {
            out.println();
            out.println("Checking ruleset: " + ruleSet.getName() + " ...");
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