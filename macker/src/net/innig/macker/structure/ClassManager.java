package net.innig.macker.structure;

import net.innig.macker.util.ClassNameTranslator;

import java.io.InputStream;
import java.util.*;

import net.innig.collect.MultiMap;
import net.innig.collect.TreeMultiMap;

public class ClassManager
    {
    public ClassManager()
        {
        allClassNames = new TreeSet();
        classNameToInfo = new HashMap();
        references = new TreeMultiMap();
        }
    
    public void addClass(ClassInfo classInfo)
        {
        allClassNames.add   (classInfo.getClassName());
        allClassNames.addAll(classInfo.getReferences());
        classNameToInfo.put (classInfo.getClassName(), classInfo);
        references.putAll   (classInfo.getClassName(), classInfo.getReferences());
        }
    
    public Set getAllClassNames()
        { return Collections.unmodifiableSet(allClassNames); }
    
    public MultiMap getReferences()
        { return references; }
//        { return InnigCollections.unmodifiableMultiMap(references); }

    public ClassInfo getClassInfo(String className)
        {
        ClassInfo classInfo = (ClassInfo) classNameToInfo.get(className);
        if(classInfo == null)
            {
            String resourceName = ClassNameTranslator.classToResourceName(className);
            InputStream classStream =
                Thread.currentThread()
                      .getContextClassLoader()
                      .getResourceAsStream(resourceName);
            
            try {
                classInfo = new ParsedClassInfo(classStream);
                classStream.close();
                }
            catch(Exception e)
                {
                System.err.println("WARNING: Cannot load class " + className
                    + "; rule checking involving its attributes may fail: " + e);
                classInfo = new IncompleteClassInfo(className);
                }
            
            addClass(classInfo);
            }
        
        return classInfo;
        }
    
    private Set/*<String>*/ allClassNames;
    private Map/*<String,ClassInfo>*/ classNameToInfo;
    private MultiMap/*<String,String>*/ references;
    }



