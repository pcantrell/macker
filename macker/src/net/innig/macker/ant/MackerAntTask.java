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

    public void addConfiguredClasses(FileSet classFiles)
        {
        DirectoryScanner classScanner = classFiles.getDirectoryScanner(getProject());
        String[] fileNames = classScanner.getIncludedFiles();
        File baseDir = classScanner.getBasedir();
        for(int n = 0; n < fileNames.length; n++)
            {
            File classFile = new File(baseDir, fileNames[n]);
            if(!classFile.getName().endsWith(".class"))
                throw new BuildException("File in <class/> fileset does not end in \".class\":"
                    + fileNames[n]);
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
