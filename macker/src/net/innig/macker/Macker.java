/*______________________________________________________________________________
 *
 * Current distribution and futher info:  http://innig.net/macker/
 *
 * Copyright 2002 Paul Cantrell
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 2.1, as published
 * by the Free Software Foundation. See the file LICENSE.html for more info.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the license for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.
 * 59 Temple Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *______________________________________________________________________________
 */
 
package net.innig.macker;

import net.innig.macker.structure.*;
import net.innig.macker.rule.*;
import net.innig.macker.event.*;

import net.innig.collect.*;

import java.io.File;
import java.util.*;

import org.jdom.input.SAXBuilder;

/**
    The main class for the command line interface.
*/
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
                    System.out.println();
                    System.out.println("macker: Unknown file type: " + args[arg]);
                    System.out.println("(expected .class or .xml)");
                    usage();
                    return;
                    }
                }
            
            if(rulesFiles.isEmpty())
                {
                System.out.println("WARNING: No rules files specified");
                usage();
                return;
                }
            
            if(cm.getPrimaryClasses().isEmpty())
                {
                System.out.println("WARNING: No class files specified");
                usage();
                return;
                }
            
            // Parsing class file
            
            if(verbose)
                {
                System.out.println(cm.getPrimaryClasses().size() + " primary classes");
                System.out.println(cm.getAllClassNames().size() + " total classes");
                System.out.println(cm.getReferences().size() + " references");
                
                for(Iterator i = cm.getPrimaryClasses().iterator(); i.hasNext(); )
                    {
                    ClassInfo classInfo = (ClassInfo) i.next();
                    System.out.println("Classes used by " + classInfo + ":");
                    for(Iterator usedIter = classInfo.getReferences().iterator(); usedIter.hasNext(); )
                        System.out.println("    " + usedIter.next());
                    System.out.println();
                    }
                }
            
            for(Iterator rfIter = rulesFiles.iterator(); rfIter.hasNext(); )
                {
                File rulesFile = (File) rfIter.next();
                if(verbose)
                    System.out.println("Reading " + rulesFile + " ...");
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
            e.printStackTrace(System.out);
            throw e;
            }
        }
        
    public static void usage()
        {
        System.out.println("usage:");
        System.out.println("    macker [-v] [rulesfile.xml | -r rulesfile | javaclass.class]+");
//            System.out.println("    macker [javalib.jar]+");
        }
    }


