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

import org.gjt.jclasslib.structures.*;
import org.gjt.jclasslib.structures.constants.ConstantClassInfo;
import org.gjt.jclasslib.io.ClassFileReader;

public class ParsedClassInfo
    extends ClassInfo
    {
    public ParsedClassInfo(File classFile)
        throws IOException, ClassParseException
        {
        try { parse(ClassFileReader.readFromFile(classFile)); }
        catch(InvalidByteCodeException ibce)
            { throw new ClassParseException (ibce); }
        }
    
    public ParsedClassInfo(InputStream classFileStream)
        throws IOException, ClassParseException
        {
        try { parse(ClassFileReader.readFromInputStream(classFileStream)); }
        catch(InvalidByteCodeException ibce)
            { throw new ClassParseException (ibce); }
        }
    
    private void parse(ClassFile classFile)
        throws ClassParseException
        {
        try {
            parseClassName(classFile);
            parseFlags(classFile);
            parseExtends(classFile);
            parseImplements(classFile);
            parseReferences(classFile);
            }
        catch(InvalidByteCodeException ibce)
            { throw new ClassParseException(ibce); }
        }
    
    private String decodeClassName(ClassFile classFile, int index)
        throws InvalidByteCodeException
        {
        if(index == 0)
            return null;
        return ClassNameTranslator.typeConstantToClassName(
            ((ConstantClassInfo) classFile.getConstantPool()[index]).getName());
        }
    
    private void parseClassName(ClassFile classFile)
        throws InvalidByteCodeException
        { className = decodeClassName(classFile, classFile.getThisClass()); }
    
    public String getClassName()
        { return className; }
    
    private void parseFlags(ClassFile classFile)
        throws InvalidByteCodeException
        {
        int flags = classFile.getAccessFlags();
        isInterface = 0 != (flags & AccessFlags.ACC_INTERFACE);
        isAbstract  = 0 != (flags & AccessFlags.ACC_ABSTRACT);
        isFinal     = 0 != (flags & AccessFlags.ACC_FINAL);
        
        int accessFlags = flags & ( AccessFlags.ACC_PUBLIC
                                  | AccessFlags.ACC_PRIVATE
                                  | AccessFlags.ACC_PROTECTED);
        if(accessFlags == AccessFlags.ACC_PUBLIC)
            accessModifier = AccessModifier.PUBLIC;
        else if(accessFlags == AccessFlags.ACC_PROTECTED)
            accessModifier = AccessModifier.PROTECTED;
        else if(accessFlags == AccessFlags.ACC_PRIVATE)
            accessModifier = AccessModifier.PRIVATE;
        else if(accessFlags == 0)
            accessModifier = AccessModifier.PACKAGE;
        else
            throw new IllegalStateException("Unknown access flags: " + accessFlags);
        }
        
    public boolean isInterface() { return isInterface; }
    public boolean isAbstract()  { return isAbstract; }
    public boolean isFinal()     { return isFinal; }

    public AccessModifier getAccessModifier()
        { return accessModifier; }

    private void parseExtends(ClassFile classFile)
        throws InvalidByteCodeException
        { extendsName = decodeClassName(classFile, classFile.getSuperClass()); }
    
    public String getExtends()
        { return extendsName; }
    
    private void parseImplements(ClassFile classFile)
        throws InvalidByteCodeException
        {
        implementsNames = new TreeSet();
        int[] interfaces = classFile.getInterfaces();
        for(int n = 0; n < interfaces.length; n++)
            implementsNames.add(decodeClassName(classFile, interfaces[n]));
        implementsNames = Collections.unmodifiableSet(implementsNames);
        }
    
    public Set/*<String>*/ getImplements()
        { return implementsNames; }
    
    private void parseReferences(ClassFile classFile)
        throws InvalidByteCodeException
        {
        CPInfo[] constantPool = classFile.getConstantPool();
        referenceNames = new TreeSet();
        apiReferenceNames = new TreeSet();
        
        // Add accessed classes from constant pool entries
        for(int n = 1; n < constantPool.length; n++)
            if(constantPool[n] instanceof ConstantClassInfo)
                referenceNames.add(
                    ClassNameTranslator.typeConstantToClassName(
                        ((ConstantClassInfo) constantPool[n]).getName()));
        
        // Add yet more accessed classes from method & field signatures
        Collection members = new ArrayList(50);
        members.addAll(Arrays.asList(classFile.getMethods()));
        members.addAll(Arrays.asList(classFile.getFields()));
        for(Iterator i = members.iterator(); i.hasNext(); )
            {
            ClassMember member = (ClassMember) i.next();
            Collection refNames =
                ClassNameTranslator.signatureToClassNames(
                    classFile.getConstantPoolUtf8Entry(member.getDescriptorIndex())
                             .getString());
            referenceNames.addAll(refNames);
            if(isApi(classFile.getAccessFlags()) && isApi(member.getAccessFlags()))
                apiReferenceNames.addAll(refNames);
            }
        
        referenceNames = Collections.unmodifiableSet(referenceNames);
        apiReferenceNames = Collections.unmodifiableSet(apiReferenceNames);
        }
    
    public Set/*<String>*/ getReferences()
        { return referenceNames; }
    
    public Set/*<String>*/ getApiReferences()
        { return apiReferenceNames; }
    
    public String toString()
        { return getClassName(); }
    
    private boolean isApi(int accessFlags)
        { return 0 != (accessFlags & (AccessFlags.ACC_PUBLIC | AccessFlags.ACC_PROTECTED)); }
    
    private String className, extendsName;
    private boolean isInterface, isAbstract, isFinal;
    private AccessModifier accessModifier;
    private Set/*<String>*/ referenceNames, apiReferenceNames, implementsNames;
    }

