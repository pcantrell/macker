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
import net.innig.collect.*;

import org.gjt.jclasslib.structures.*;
import org.gjt.jclasslib.structures.attributes.ExceptionsAttribute;
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
            { throw new ClassParseException(ibce); }
        }
    
    public ParsedClassInfo(InputStream classFileStream)
        throws IOException, ClassParseException
        {
        try { parse(ClassFileReader.readFromInputStream(classFileStream)); }
        catch(InvalidByteCodeException ibce)
            { throw new ClassParseException(ibce); }
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
        throws ClassParseException, InvalidByteCodeException
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
        throws ClassParseException, InvalidByteCodeException
        { extendsName = decodeClassName(classFile, classFile.getSuperClass()); }
    
    public String getExtends()
        { return extendsName; }
    
    private void parseImplements(ClassFile classFile)
        throws ClassParseException, InvalidByteCodeException
        {
        implementsNames = new TreeSet();
        int[] interfaces = classFile.getInterfaces();
        for(int a = 0; a < interfaces.length; a++)
            implementsNames.add(decodeClassName(classFile, interfaces[a]));
        implementsNames = Collections.unmodifiableSet(implementsNames);
        }
    
    public Set/*<String>*/ getImplements()
        { return implementsNames; }
    
    private void parseReferences(ClassFile classFile)
        throws ClassParseException, InvalidByteCodeException
        {
        CPInfo[] constantPool = classFile.getConstantPool();
        references = new CompositeMultiMap(TreeMap.class, HashSet.class);
        
        // Add accessed classes from constant pool entries
        for(int a = 1; a < constantPool.length; a++)
            if(constantPool[a] instanceof ConstantClassInfo)
                addReference(
                    new Reference(
                        getClassName(),
                        ClassNameTranslator.typeConstantToClassName(
                            ((ConstantClassInfo) constantPool[a]).getName()),
                        ReferenceType.CONSTANT_POOL,
                        null,
                        null));
        
        // Add yet more accessed classes from method & field signatures
        MethodInfo[] methods = classFile.getMethods();
        for(int m = 0; m < methods.length; m++)
            {
            MethodInfo method = methods[m];
            AccessModifier methodAccess = translateAccess(method.getAccessFlags());
            
            List paramsAndReturn =
                ClassNameTranslator.signatureToClassNames(
                    classFile.getConstantPoolUtf8Entry(method.getDescriptorIndex())
                             .getString());
            if(paramsAndReturn.isEmpty())
                throw new ClassParseException(
                    "unable to read types for method " + className + '.' + method.getName());
            
            for(Iterator i = paramsAndReturn.iterator(); i.hasNext(); )
                {
                String refTo = (String) i.next();
                addReference(
                    new Reference(
                        getClassName(),
                        (String) paramsAndReturn.get(paramsAndReturn.size()-1),
                        i.hasNext() ? ReferenceType.METHOD_PARAM
                                    : ReferenceType.METHOD_RETURNS,
                        method.getName(),
                        methodAccess));
                }
            
            AttributeInfo[] attribs = method.getAttributes();
            for(int a = 0; a < attribs.length; a++)
                if(attribs[a] instanceof ExceptionsAttribute)
                    {
                    int[] excepts = ((ExceptionsAttribute) attribs[a]).getExceptionIndexTable();
                    for(int e = 0; e < excepts.length; e++)
                        addReference(
                            new Reference(
                                getClassName(),
                                ClassNameTranslator.typeConstantToClassName(
                                    classFile.getConstantPoolEntryName(excepts[e])),
                                ReferenceType.METHOD_THROWS,
                                method.getName(),
                                methodAccess));
                    }
            }
        
        FieldInfo[] fields = classFile.getFields();
        for(int a = 0; a < fields.length; a++)
            {
            FieldInfo field = fields[a];
            List types =
                ClassNameTranslator.signatureToClassNames(
                    classFile.getConstantPoolUtf8Entry(field.getDescriptorIndex())
                             .getString());
            if(types.size() != 1)
                throw new ClassParseException(
                    "expected one type for field " + className + '.' + field.getName()
                    + "; got: " + types);

            addReference(
                new Reference(
                    getClassName(),
                    (String) types.get(0),
                    ReferenceType.FIELD,
                    field.getName(),
                    translateAccess(field.getAccessFlags())));
            }

        references = InnigCollections.unmodifiableMultiMap(references);
        }
        
    private AccessModifier translateAccess(int accessFlags)
        throws ClassParseException
        {
        accessFlags = accessFlags & ( AccessFlags.ACC_PUBLIC
                                    | AccessFlags.ACC_PROTECTED
                                    | AccessFlags.ACC_PRIVATE);
        if(accessFlags == AccessFlags.ACC_PUBLIC)
            return AccessModifier.PUBLIC;
        if(accessFlags == AccessFlags.ACC_PROTECTED)
            return AccessModifier.PROTECTED;
        if(accessFlags == 0)
            return AccessModifier.PACKAGE;
        if(accessFlags == AccessFlags.ACC_PRIVATE)
            return AccessModifier.PRIVATE;
        throw new ClassParseException("unknown access flags: " + accessFlags);
        }
    
    private void addReference(Reference ref)
        { references.put(ref.getTo(), ref); }
    
    public MultiMap/*<String,Reference>*/ getReferences()
        { return references; }
    
    public String toString()
        { return getClassName(); }
    
    private boolean isApi(int accessFlags)
        { return 0 != (accessFlags & (AccessFlags.ACC_PUBLIC | AccessFlags.ACC_PROTECTED)); }
    
    private String className, extendsName;
    private boolean isInterface, isAbstract, isFinal;
    private AccessModifier accessModifier;
    private Set/*<String>*/ implementsNames;
    private MultiMap/*<String,Reference>*/ references;
    }

