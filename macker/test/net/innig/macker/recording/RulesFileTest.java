/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2003 Paul Cantrell
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
 
package net.innig.macker.recording;

import net.innig.macker.Macker;
import net.innig.macker.rule.*;
import net.innig.macker.event.*;
import net.innig.macker.structure.*;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.AssertionFailedError;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;


public final class RulesFileTest
    implements Test
    {
    public static final String
        TEST_DIR_PROP  = "Macker.RulesFileTest.testDir",
        BUILD_DIR_PROP = "Macker.RulesFileTest.buildDir";
    
    public static TestSuite suite()
        {
        String testDirS = System.getProperty(TEST_DIR_PROP);
        String buildDirS = System.getProperty(BUILD_DIR_PROP);
        if(testDirS == null)
            throw new IllegalStateException("System property " + TEST_DIR_PROP + " not set");
        if(buildDirS == null)
            throw new IllegalStateException("System property " + BUILD_DIR_PROP + " not set");
        
        File testDir = new File(testDirS);
        File buildDir = new File(buildDirS);
        if(!testDir.isDirectory())
            throw new IllegalArgumentException(testDir + " is not a directory");
        if(!buildDir.isDirectory())
            throw new IllegalArgumentException(buildDir + " is not a directory");
        
        TestSuite suite = new TestSuite("Rules File Tests");
        File[] testFiles = testDir.listFiles();
        for(int f = 0; f < testFiles.length; f++)
            if(testFiles[f].getName().endsWith(".xml"))
                suite.addTest(new RulesFileTest(testFiles[f], buildDir));
                
        return suite;
        }
    
    public RulesFileTest(File testFile, File buildDir)
        {
        if(!buildDir.isDirectory())
            throw new IllegalArgumentException(buildDir + " is not a directory");

        this.testFile = testFile;
        this.buildDir = buildDir;
        this.name = testFile.getName();
        if(name.endsWith(".xml"))
            name = name.substring(0, name.lastIndexOf(".xml"));
        }

    public int countTestCases()
        { return 1; }
    
    public void run(TestResult result)
        {
        result.startTest(this);

        try {
            build();
            run();
            }
        catch(AssertionFailedError e)
            { result.addFailure(this, e); }
        catch(Exception e)
            { result.addError(this, e); }
        
        result.endTest(this);
        }
    
    private void run()
        throws MackerIsMadException, ListenerException, RulesException,
               IOException, ClassParseException, AssertionFailedError
        {
        Macker macker = new Macker();

        RecordingListener recordingListener = new RecordingListener();
        macker.addListener(recordingListener);
        
        for(Iterator rsIter = rulesFile.iterator(); rsIter.hasNext();)
            macker.addRuleSet((RuleSet) rsIter.next());
        for(Iterator cfIter = classFiles.iterator(); cfIter.hasNext();)
            macker.addClass((File) cfIter.next());
        
        macker.checkRaw();
        
        EventRecording actual = recordingListener.getRecording();
        
        StringWriter mismatches = new StringWriter();
        PrintWriter out = new PrintWriter(mismatches);
        out.println("Mismatched events:");
        if(!expected.compare(actual, out))
            {
            out.println();
            out.println("Excepted:");
            expected.dump(out, 4);
            out.println();
            out.println("Actual:");
            actual.dump(out, 4);
            out.flush();
            throw new AssertionFailedError(mismatches.toString());
            }
        }
    
    private void dump(Collection c)
        {
        for(Iterator i = c.iterator(); i.hasNext(); )
            System.out.println("   " + i.next());
        }
    
    public String toString()
        { return name; }
    
    // -----------------------------------------------------------------
    
    private void build()
        throws Exception
        {
        System.out.println("Setting up test case: " + this + " ...");
        
        SAXBuilder saxBuilder = new SAXBuilder(false);
        Element rootElem = saxBuilder.build(testFile).getRootElement();
        
        buildTestClasses(rootElem.getChild("test-classes"));
        buildRulesFile(rootElem.getChild("rules-file"));
        buildExpectedEvents(rootElem.getChild("expected-events"));
        }
    
    private void buildTestClasses(Element testClassesElem)
        throws Exception
        {
        File baseDir = new File(buildDir, name + "-" + System.currentTimeMillis());
        File srcDir = new File(baseDir, "src");
        File classesDir = new File(baseDir, "classes");
        
        List/*<String>*/ javacArgs = new ArrayList();
        javacArgs.add("-d");
        javacArgs.add(classesDir.getPath());
        classesDir.mkdirs();
        
        System.out.println("Generating source files ...");
        for(Iterator sourceIter = testClassesElem.getChildren("source").iterator(); sourceIter.hasNext(); )
            {
            Element sourceElem = (Element) sourceIter.next();
            String packName = sourceElem.getAttributeValue("package");
            String className = sourceElem.getAttributeValue("class");
            packName = (packName == null) ? "" : packName + ".";
            
            String sourceCode = sourceElem.getText();
            
            char pathSeparatorChar =
                System.getProperty("os.name").toLowerCase().equals("mac os x")
                    ? '/'    // erroneously comes out as ':' on OS X -- shame on Apple!
                    : File.pathSeparatorChar;
            File sourceFile = new File(
                srcDir,
                packName.replace('.', pathSeparatorChar) + className + ".java");
            sourceFile.getParentFile().mkdirs();

            BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile));
            try {
                out.write(sourceCode);
                out.flush();
                }
            finally
                {
                try { out.close(); }
                catch(Exception e) { }
                }
            javacArgs.add(sourceFile.getPath());
            }

        System.out.println("Compiling ...");

        int compilerResult = com.sun.tools.javac.Main.compile(
            (String[]) javacArgs.toArray(new String[0]));
        if(compilerResult != 0)
            throw new Exception("compile failed (result code " + compilerResult + ")");
        
        classFiles = new ArrayList();
        findFiles(classesDir, classFiles);
        }
    
    private void findFiles(File file, Collection files)
        {
        if(file.isDirectory())
            {
            File[] contents = file.listFiles();
            for(int f = 0; f < contents.length; f++)
                findFiles(contents[f], files);
            }
        else
            files.add(file);
        }
    
    private void buildRulesFile(Element rulesFileElem)
        throws RulesException
        { rulesFile = new RuleSetBuilder().build(rulesFileElem); }
    
    private void buildExpectedEvents(Element expectedEventsElem)
        {
        expected = new RuleSetRecording(null);
        expected.read(expectedEventsElem);
        }
    
    // -----------------------------------------------------------------
    
    private String name;
    private File testFile;
    private File buildDir;
    
    private Collection/*<RuleSet>*/ rulesFile;
    private Collection/*<File>*/ classFiles;

    private EventRecording expected;
    }



