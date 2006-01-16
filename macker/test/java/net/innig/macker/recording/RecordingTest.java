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
import net.innig.macker.event.ListenerException;
import net.innig.macker.event.MackerIsMadException;
import net.innig.macker.rule.RuleSet;
import net.innig.macker.rule.RuleSetBuilder;
import net.innig.macker.rule.RulesException;
import net.innig.macker.structure.ClassParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;


public final class RecordingTest
    implements Test
    {
    public static final String
        TEST_DIR_PROP  = "Macker.RecordingTest.testDir",
        BUILD_DIR_PROP = "Macker.RecordingTest.buildDir";
    
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
        
        File[] testFiles = testDir.listFiles();
        
        Arrays.sort(  // most recent first
            testFiles,
            new Comparator<File>()
                {
                public int compare(File f1, File f2)
                    {
                    long diff = f2.lastModified() - f1.lastModified();
                    return diff > 0 ? 1 : -1;
                    }
                });
        
        TestSuite suite = new TestSuite("Rules File Tests");
        for(int f = 0; f < testFiles.length; f++)
            if(testFiles[f].getName().endsWith(".xml"))
                suite.addTest(new RecordingTest(testFiles[f], buildDir));
                
        return suite;
        }
    
    public RecordingTest(File testFile, File buildDir)
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

        System.out.println(this + " ...");
        
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
        
        for(RuleSet rs : rulesFile)
            macker.addRuleSet(rs);
        for(File cf : classFiles)
            macker.addClass(cf);
        
        macker.setPrintThreshold(null);
        
        Collection actualAngerEvents;
        try {
            macker.check();
            actualAngerEvents = Collections.EMPTY_LIST;
        } catch(MackerIsMadException mime) {
            actualAngerEvents = mime.getEvents();
        }
        
        EventRecording actual = recordingListener.getRecording();
        
        StringWriter mismatches = new StringWriter();
        PrintWriter out = new PrintWriter(mismatches);
        out.println("Mismatched events:");
        if(!expected.compare(actual, out))
            {
            System.out.println();
            System.out.println("Excepted:");
            expected.dump(System.out, 4);
            System.out.println();
            System.out.println("Actual:");
            actual.dump(System.out, 4);
            throw new AssertionFailedError(mismatches.toString());
            }
        if(expectedAngerEvents >= 0 && actualAngerEvents.size() != expectedAngerEvents)
            throw new AssertionFailedError(
                "Expected " + expectedAngerEvents + " anger events, but got " + actualAngerEvents.size() + ": " + actualAngerEvents);
        }
    
    public String getName()
        { return "RecordingTest \"" + name + '"'; }
    
    public String toString()
        { return getName(); }
    
    // -----------------------------------------------------------------
    
    private void build()
        throws Exception
        {
        SAXBuilder saxBuilder = new SAXBuilder(false);
        Element rootElem = saxBuilder.build(testFile).getRootElement();
        
        buildTestClasses(rootElem.getChild("test-classes"));
        buildRulesFile(rootElem.getChild("rules-file"));
        buildExpectedEvents(rootElem.getChild("expected-events"));
        }
    
    private void buildTestClasses(Element testClassesElem)
        throws Exception
        {
        File baseDir = new File(buildDir, name);
        File srcDir = new File(baseDir, "src");
        File classesDir = new File(baseDir, "classes");
        
        List<String> javacArgs = new ArrayList<String>();
        javacArgs.add("-d");
        javacArgs.add(classesDir.getPath());
        classesDir.mkdirs();
        
        for(Element sourceElem : (List<Element>) testClassesElem.getChildren("source"))
            {
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

        int compilerResult = com.sun.tools.javac.Main.compile(
            javacArgs.toArray(new String[0]));
        if(compilerResult != 0)
            throw new Exception("compile failed (result code " + compilerResult + ")");
        
        classFiles = new ArrayList<File>();
        findFilesDeep(classesDir, classFiles);
        }
    
    private void findFilesDeep(File file, Collection<File> results)
        {
        if(file.isDirectory())
            {
            File[] contents = file.listFiles();
            for(int f = 0; f < contents.length; f++)
                findFilesDeep(contents[f], results);
            }
        else
            results.add(file);
        }
    
    private void buildRulesFile(Element rulesFileElem)
        throws RulesException
        { rulesFile = new RuleSetBuilder().build(rulesFileElem); }
    
    private void buildExpectedEvents(Element expectedEventsElem)
        {
        expected = new RuleSetRecording(null);
        expected.read(expectedEventsElem);
        expectedAngerEvents = Integer.parseInt(expectedEventsElem.getAttributeValue("expectedAngerEvents", "-1"));
        }
    
    // -----------------------------------------------------------------
    
    private String name;
    private File testFile;
    private File buildDir;
    
    private Collection<RuleSet> rulesFile;
    private Collection<File> classFiles;

    private EventRecording expected;
    private int expectedAngerEvents;
    }



