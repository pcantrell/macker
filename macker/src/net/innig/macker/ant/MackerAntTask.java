/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002 Paul Cantrell
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

import java.util.*;
import java.io.File;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.taskdefs.Java;

/** 
    A task which allows access to Macker from Ant build files.
    
    @see <a href="http://jakarta.apache.org/ant/manual/develop.html">The Ant manual</a>
*/
public class MackerAntTask extends Task
    {
    public MackerAntTask()
        {
        args = new ArrayList();
        }
        
    public void execute()
        throws BuildException
        {
        if(verbose)
            System.out.println("Macker:");
        jvm.setTaskName("macker");
        jvm.setClassname("net.innig.macker.Macker");
        jvm.setFork(fork);
        jvm.setFailonerror(failOnError);
        jvm.clearArgs();
        for(Iterator i = args.iterator(); i.hasNext(); )
            jvm.createArg().setValue((String) i.next());
        try
            { jvm.execute(); }
        catch(BuildException be)
            {
            // Any necessary stack trace was already reported to stderr
            throw new BuildException("Macker rules checking failed");
            }
        }

    public void setFork(boolean fork)
        { this.fork = fork;  }

    public void setFailOnError(boolean failOnError)
        { this.failOnError = true; }

    public void setVerbose(boolean verbose)
        {
        this.verbose = verbose;
        if(verbose)
            args.add("-v");
        }

    public Path createClasspath()
        { return getJvm().createClasspath(); }

    public void addConfiguredVar(Var var)
        {
        args.add("-D");
        args.add(var.getName() + "=" + var.getValue());
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
            args.add(classFile.getPath());
            }
        }

    public void addConfiguredRules(FileSet rulesFiles)
        {
        DirectoryScanner rulesScanner = rulesFiles.getDirectoryScanner(getProject());
        String[] fileNames = rulesScanner.getIncludedFiles();
        File baseDir = rulesScanner.getBasedir();
        for(int n = 0; n < fileNames.length; n++)
            {
            args.add("-r");
            args.add(new File(baseDir, fileNames[n]).getPath());
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

    private boolean fork = true;
    private boolean failOnError = true;
    private boolean verbose = false;
    private List/*<File>*/ rulesFiles;
    private List/*<String>*/ args;
    private Java jvm;
    }
