package net.innig.macker.structure;

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
        { return (ClassInfo) classNameToInfo.get(className); }
    
    private Set/*<String>*/ allClassNames;
    private Map/*<String,ClassInfo>*/ classNameToInfo;
    private MultiMap/*<String,String>*/ references;
    }



