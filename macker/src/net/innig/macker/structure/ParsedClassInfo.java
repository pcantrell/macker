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

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;

import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.CPInfo;
import org.gjt.jclasslib.structures.constants.ConstantClassInfo;
import org.gjt.jclasslib.io.ClassFileReader;
import org.gjt.jclasslib.structures.InvalidByteCodeException;

public class ParsedClassInfo
    extends ClassInfo
    {
    public ParsedClassInfo(File classFile)
        throws IOException, InvalidByteCodeException
        { this(ClassFileReader.readFromFile(classFile)); }
    
    public ParsedClassInfo(InputStream classFileStream)
        throws IOException, InvalidByteCodeException
        { this(ClassFileReader.readFromInputStream(classFileStream)); }
    
    private ParsedClassInfo(ClassFile classFile)
        throws InvalidByteCodeException
        {
        CPInfo[] constantPool = classFile.getConstantPool();
        className =
            ClassNameTranslator.typeConstantToClassName(
                ((ConstantClassInfo) constantPool[classFile.getThisClass()]).getName());
        references = new TreeSet();
        for(int n = 1; n < constantPool.length; n++)
            if(constantPool[n] instanceof ConstantClassInfo)
                references.add(
                    ClassNameTranslator.typeConstantToClassName(
                        ((ConstantClassInfo) constantPool[n]).getName()));
        references = Collections.unmodifiableSet(references);
        }
    
    public String getClassName()
        { return className; }
    
    public Set/*<String>*/ getReferences()
        { return references; }
    
    public String toString()
        { return getClassName(); }
    
    public String className;
    private Set/*<String>*/ references;
    }



