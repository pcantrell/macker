/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
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
        primaryClasses = new HashSet();
        classNameToInfo = new HashMap();
        references = new TreeMultiMap();
        }
    
    public void addClass(ClassInfo classInfo, boolean primary)
        {
        allClassNames.add   (classInfo.getClassName());
        classNameToInfo.put (classInfo.getClassName(), classInfo);
        if(primary)
            {
            primaryClasses.add  (classInfo);
            references.putAll   (classInfo.getClassName(), classInfo.getReferences());
            allClassNames.addAll(classInfo.getReferences());
            }
        }
    
    public Set/*<String>*/ getAllClassNames()
        { return Collections.unmodifiableSet(allClassNames); }
    
    public Set/*<ClassInfo>*/ getPrimaryClasses()
        { return Collections.unmodifiableSet(primaryClasses); }
    
    public MultiMap/*<String,String>*/ getReferences()
        { return references; }
//        { return InnigCollections.unmodifiableMultiMap(references); }

    public ClassInfo getClassInfo(String className)
        {
        ClassInfo classInfo = (ClassInfo) classNameToInfo.get(className);
        if(classInfo == null)
            {
            classInfo = PrimitiveTypeInfo.getPrimitiveTypeInfo(className);
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
                    if(!incompleteClassWarning)
                        {
                        incompleteClassWarning = true;
                        System.out.println("WARNING: Macker is unable to find some of the classes"
                            + " accessed by the input classes (see messages below).  Rules which"
                            + " depend on attributes of these classes other than their names will"
                            + " fail.  Check your classpath.");
                        }
                    System.out.println("Cannot load class " + className);
                    classInfo = new IncompleteClassInfo(className);
                    }
                }
            
            addClass(classInfo, false);
            }
        
        return classInfo;
        }
    
    private boolean incompleteClassWarning;
    private Set/*<String>*/ allClassNames, primaryClasses;
    private Map/*<String,ClassInfo>*/ classNameToInfo;
    private MultiMap/*<String,String>*/ references;
    }



