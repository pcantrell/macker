/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002-2003 Paul Cantrell
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
 
package net.innig.macker.ant;

import net.innig.macker.Macker;
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.rule.RuleSeverity;
import net.innig.macker.rule.RulesException;
import net.innig.macker.structure.ClassParseException;
import net.innig.macker.structure.IncompleteClassInfoException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

/** 
    A task which allows access to Macker from Ant build files.
    
    @see <a href="http://ant.apache.org/manual/">The Ant manual</a>
*/
public class MackerAntTask extends Task
    {
    public MackerAntTask()
        {
        macker = new Macker();
        jvmArgs = new ArrayList();
        }
        
    public void execute()
        throws BuildException
        {
        if(verbose)
            System.out.println("Macker (verbose mode enabled)");
        if(failOnError && angerProperty != null)
            System.out.println("WARNING: failOnError is set, so angerProperty will have no effect");

        try
            { 
            if(!fork)
                {
                if(classPath != null)
                    macker.setClassLoader(new AntClassLoader(getProject(), classPath, false));
                
                macker.check();
                }
            else
                {
                if(classPath == null)
                    throw new BuildException("nested <classpath> element is required when fork=true");
                
                getJvm().setTaskName("macker");
                getJvm().setClassname("net.innig.macker.Macker");
                getJvm().setFork(fork);
                getJvm().setFailonerror(false);
                getJvm().clearArgs();
                
                for(Iterator i = jvmArgs.iterator(); i.hasNext(); )
                    getJvm().createArg().setValue((String) i.next());
                    
                int resultCode = getJvm().executeJava();
                if(resultCode == 2)
                    throw new MackerIsMadException();
                if(resultCode != 0)
                    throw new BuildException(MACKER_CHOKED_MESSAGE);
                }
            }
        catch(MackerIsMadException mime)
            {
            if(mime.getMessage() != null)
                {
                System.out.println();
                System.out.println(mime.getMessage());
                }
            if(angerProperty != null)
                getProject().setProperty(angerProperty, "true");
            if(failOnError)
                throw new BuildException(MACKER_IS_MAD_MESSAGE);
            }
        catch(ListenerException lie)
            {
            System.out.println();
            System.out.println(lie.getMessage());
            throw new BuildException(MACKER_CHOKED_MESSAGE);
            }
        catch(RulesException rue)
            {
            System.out.println();
            System.out.println(rue.getMessage());
            throw new BuildException(MACKER_CHOKED_MESSAGE);
            }
        catch(IncompleteClassInfoException icie)
            {
            System.out.println();
            System.out.println(icie.getMessage());
            throw new BuildException(MACKER_CHOKED_MESSAGE);
            }
        }

    public void setFork(boolean fork)
        { this.fork = fork;  }

    public void setFailOnError(boolean failOnError)
        { this.failOnError = failOnError; }
    
    public void setMaxMessages(int printMaxMessages)
        {
        macker.setPrintMaxMessages(printMaxMessages);
        jvmArgs.add("--print-max");
        jvmArgs.add(String.valueOf(printMaxMessages));
        }

    public void setPrintThreshold(String threshold)
        {
        macker.setPrintThreshold(RuleSeverity.fromName(threshold));
        jvmArgs.add("--print");
        jvmArgs.add(threshold);
        }

    public void setAngerThreshold(String threshold)
        {
        macker.setAngerThreshold(RuleSeverity.fromName(threshold));
        jvmArgs.add("--anger");
        jvmArgs.add(threshold);
        }

    public void setAngerProperty(String property)
        { this.angerProperty = property; }

    public void setVerbose(boolean verbose)
        {
        this.verbose = verbose;
        macker.setVerbose(verbose);
        if(verbose)
            jvmArgs.add("-v");
        }

    public Path createClasspath()
        { return classPath = getJvm().createClasspath(); }

    public void addConfiguredVar(Var var)
        {
        macker.setVariable(var.getName(), var.getValue());
        jvmArgs.add("-D");
        jvmArgs.add(var.getName() + "=" + var.getValue());
        }

    public void setXmlReportFile(File xmlReportFile)
        {
        macker.setXmlReportFile(xmlReportFile);
        jvmArgs.add("-o");
        jvmArgs.add(xmlReportFile.getPath());
        }
    
    static public class Var
        {
        public String getName() { return name; }
        public String getValue() { return value; }
        public void setName(String name) { this.name = name; }
        public void setValue(String value) { this.value = value; }
        private String name, value;
        }

    public void addConfiguredClasses(FileSet classFiles)
        throws IOException
        {
        DirectoryScanner classScanner = classFiles.getDirectoryScanner(getProject());
        String[] fileNames = classScanner.getIncludedFiles();
        File baseDir = classScanner.getBasedir();
        for(int n = 0; n < fileNames.length; n++)
            {
            File classFile = new File(baseDir, fileNames[n]);
            if(!classFile.getName().endsWith(".class"))
                System.out.println("WARNING: " + fileNames[n]
                    + " is not a .class file; ignoring");
            jvmArgs.add(classFile.getPath());
            try
                { macker.addClass(classFile); }
            catch(ClassParseException cpe)
                {
                System.out.println("Unable to parse class file: " + classFile.getPath());
                System.out.println(cpe.getMessage());
                throw new BuildException(MACKER_CHOKED_MESSAGE);
                }
            }
        }

    public void addConfiguredRules(FileSet rulesFiles)
        throws IOException
        {
        DirectoryScanner rulesScanner = rulesFiles.getDirectoryScanner(getProject());
        String[] fileNames = rulesScanner.getIncludedFiles();
        File baseDir = rulesScanner.getBasedir();
        for(int n = 0; n < fileNames.length; n++)
            {
            File rulesFile = new File(baseDir, fileNames[n]);
            jvmArgs.add("-r");
            jvmArgs.add(rulesFile.getPath());
            try
                { macker.addRulesFile(rulesFile); }
            catch(RulesException re)
                {
                System.out.println(re.getMessage());
                throw new BuildException(MACKER_CHOKED_MESSAGE);
                }
            }
        }
    
    private Java getJvm()
        {
        if(jvm == null)
            {
            jvm = new Java();
            jvm.setProject(getProject());
            }
        return jvm;
        }

    private boolean fork = false;
    private boolean failOnError = true;
    private boolean verbose = false;
    private List/*<String>*/ jvmArgs;
    private Macker macker;  // for non-forked
    private Java jvm;       // for forked
    private Path classPath;
    private String angerProperty;
    
    private static final String MACKER_CHOKED_MESSAGE = "Macker configuration failed";
    private static final String MACKER_IS_MAD_MESSAGE = "Macker rules checking failed";
    }
