package net.innig.macker;

import net.innig.macker.structure.ClassManager;
import net.innig.macker.structure.ClassInfo;
import net.innig.macker.rule.RuleSetBuilder;

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
        
        for(Iterator i = cm.getReferences().keySet().iterator(); i.hasNext(); )
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
        }
    }