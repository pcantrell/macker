package net.innig.macker;

import net.innig.macker.structure.*;
import net.innig.macker.rule.*;

import net.innig.collect.*;

import java.io.File;
import java.util.*;

import org.jdom.input.SAXBuilder;

public class Macker
    {
    public static void main(String[] args)
        throws Exception
        {
        // Parsing class file
        
        ClassManager cm = new ClassManager();
        for(int arg = 0; arg < args.length; arg++)
            cm.addClass(new ClassInfo(new File(args[arg])));
        
        Set allClasses = cm.getReferences().keySet();
        for(Iterator i = allClasses.iterator(); i.hasNext(); )
            {
            String className = (String) i.next();
            System.out.println("Classes used by " + className + ":");
            for(Iterator usedIter = cm.getReferences().get(className).iterator(); usedIter.hasNext(); )
                System.out.println("    " + usedIter.next());
            System.out.println();
            }
        
        System.out.println(cm.getAllClassNames().size() + " total classes");
        System.out.println(cm.getReferences().keySet().size() + " referencing classes");
        System.out.println(cm.getReferences().size() + " total references");
        
        Collection ruleSets = new RuleSetBuilder().build("macker-sample.xml");
        for(Iterator rsIter = ruleSets.iterator(); rsIter.hasNext(); )
            {
            RuleSet rs = (RuleSet) rsIter.next();
            for(Iterator patIter = rs.getAllPatterns().iterator(); patIter.hasNext(); )
                {
                final Pattern pat = (Pattern) patIter.next();
                final EvaluationContext ctx = new EvaluationContext(rs);
                System.out.println("matching " + pat);
                for(Iterator i = allClasses.iterator(); i.hasNext(); )
                    {
                    ClassInfo classInfo = cm.getClassInfo((String) i.next());
                    if(pat.matches(ctx, classInfo))
                        System.out.println("    " + classInfo);
                    }
                System.out.println();
                }
            }
        }
    }