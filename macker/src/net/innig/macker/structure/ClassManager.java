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
        // Trees make nice sorted output
        allClassNames = new TreeSet();
        primaryClasses = new TreeSet();
        classNameToInfo = new TreeMap();
        references = new TreeMultiMap();
        classLoader = Thread.currentThread().getContextClassLoader();
        }
    
    public ClassLoader getClassLoader()
        { return classLoader; }
    
    public void setClassLoader(ClassLoader classLoader)
        { this.classLoader = classLoader; }
    
    public void addClass(ClassInfo classInfo, boolean primary)
        {
        if(primary && classInfo instanceof IncompleteClassInfo)
            throw new IncompleteClassInfoException(
                classInfo.getClassName() + " cannot be a primary class, because the class"
                + " file isn't on Macker's classpath");
        allClassNames.add   (classInfo.getClassName());
        classNameToInfo.put (classInfo.getClassName(), classInfo);
        if(primary)
            {
            primaryClasses.add  (classInfo);
            references.putAll   (classInfo.getClassName(), classInfo.getReferences());
            allClassNames.addAll(classInfo.getReferences());
            }
        }
    
    public void makePrimary(String className)
        { addClass(getClassInfo(className), true); }
    
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
                InputStream classStream = classLoader.getResourceAsStream(resourceName);
                
                try {
                    classInfo = new ParsedClassInfo(classStream);
                    classStream.close();
                    }
                catch(Exception e)
                    {
                    if(!incompleteClassWarning)
                        {
                        incompleteClassWarning = true;
                        System.out.println(
                            "WARNING: Macker is unable to find some of the external classes"
                            + " used by the primary classes (see warnings below).  Rules which"
                            + " depend on attributes of these missing classes other than their"
                            + " names will fail.  Check your classpath.");
                        }
                    System.out.println("WARNING: Cannot load class " + className);
                    classInfo = new IncompleteClassInfo(className);
                    }
                }
            
            addClass(classInfo, false);
            }
        
        return classInfo;
        }
    
    private boolean incompleteClassWarning;
    private ClassLoader classLoader;
    private Set/*<String>*/ allClassNames, primaryClasses;
    private Map/*<String,ClassInfo>*/ classNameToInfo;
    private MultiMap/*<String,String>*/ references;
    }



