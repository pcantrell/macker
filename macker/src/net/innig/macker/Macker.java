package net.innig.macker;

import net.innig.macker.structure.*;
import net.innig.macker.rule.*;
import net.innig.macker.event.*;

import net.innig.collect.*;

import java.io.File;
import java.util.*;

import org.jdom.input.SAXBuilder;

public class Macker
    {
    public static void main(String[] args)
        throws Exception
        {
        try
            {
            // Parse args
            
            ClassManager cm = new ClassManager();
            List rulesFiles = new ArrayList();
            boolean verbose = false;
            
            boolean nextIsRule = false;
            for(int arg = 0; arg < args.length; arg++)
                {
                if(args[arg].equals("-h")
                || args[arg].equals("-help")
                || args[arg].equals("--help"))
                    {
                    usage();
                    return;
                    }
                else if(args[arg].equals("-v"))
                    verbose = true;
                else if(args[arg].equals("-r"))
                    nextIsRule = true;
                else if(args[arg].endsWith(".xml") || nextIsRule)
                    {
                    rulesFiles.add(new File(args[arg]));
                    nextIsRule = false;
                    }
                else if(args[arg].endsWith(".class"))
                    cm.addClass(new ParsedClassInfo(new File(args[arg])), true);
                else
                    {
                    System.err.println();
                    System.err.println("macker: Unknown file type: " + args[arg]);
                    System.err.println("(expected .class or .xml)");
                    usage();
                    return;
                    }
                }
            
            if(rulesFiles.isEmpty())
                {
                System.err.println("WARNING: No rules files specified");
                usage();
                return;
                }
            
            if(cm.getPrimaryClasses().isEmpty())
                {
                System.err.println("WARNING: No class files specified");
                usage();
                return;
                }
            
            // Parsing class file
            
            if(verbose)
                for(Iterator i = cm.getPrimaryClasses().iterator(); i.hasNext(); )
                    {
                    ClassInfo classInfo = (ClassInfo) i.next();
                    System.out.println("Classes used by " + classInfo + ":");
                    for(Iterator usedIter = classInfo.getReferences().iterator(); usedIter.hasNext(); )
                        System.out.println("    " + usedIter.next());
                    System.out.println();
                    }
            
            System.out.println(cm.getPrimaryClasses().size() + " primary classes");
            System.out.println(cm.getAllClassNames().size() + " total classes");
            System.out.println(cm.getReferences().size() + " references");
            
            for(Iterator rfIter = rulesFiles.iterator(); rfIter.hasNext(); )
                {
                File rulesFile = (File) rfIter.next();
                if(verbose)
                    System.err.println("Reading " + rulesFile + " ...");
                Collection ruleSets = new RuleSetBuilder().build(rulesFile);
                for(Iterator rsIter = ruleSets.iterator(); rsIter.hasNext(); )
                    {
                    RuleSet rs = (RuleSet) rsIter.next();
                    
                    if(verbose)
                        for(Iterator patIter = rs.getAllPatterns().iterator(); patIter.hasNext(); )
                            {
                            final Pattern pat = (Pattern) patIter.next();
                            final EvaluationContext ctx = new EvaluationContext(rs);
                            System.out.println("matching " + pat);
                            for(Iterator i = cm.getPrimaryClasses().iterator(); i.hasNext(); )
                                {
                                ClassInfo classInfo = (ClassInfo) i.next();
                                if(pat.matches(ctx, classInfo))
                                    System.out.println("    " + classInfo);
                                }
                            System.out.println();
                            }
                    
                    EvaluationContext context = new EvaluationContext(rs);
                    context.addListener(new PrintingListener(System.out));
                    context.addListener(new ThrowingListener(false));
                    rs.check(context, cm);
                    }
                }
            }
        catch(MackerIsMadException mime)
            { System.exit(2); }
        catch(Exception e)
            {
            e.printStackTrace(System.err);
            throw e;
            }
        }
        
        public static void usage()
            {
            System.err.println("usage:");
            System.err.println("    macker [-v] [<rules-file>.xml|-r <rules-file>]+ [<javaclass>.class]+");
            System.err.println("    macker [<javalib>.jar]+");
            }
    }


