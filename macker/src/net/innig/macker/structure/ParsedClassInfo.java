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

import org.apache.bcel.classfile.*;

public class ParsedClassInfo
    extends AbstractClassInfo
    {
    public ParsedClassInfo(ClassManager classManager, File classFile)
        throws IOException, ClassParseException
        {
        super(classManager);
        try { parse(new ClassParser(classFile.getPath()).parse()); }
        catch(ClassFormatError cfe)
            { throw new ClassParseException(cfe); }
        }
    
    public ParsedClassInfo(ClassManager classManager, InputStream classFileStream)
        throws IOException, ClassParseException
        {
        super(classManager);
        try { parse(new ClassParser(classFileStream, null).parse()); }
        catch(ClassFormatError cfe)
            { throw new ClassParseException(cfe); }
        }
    
    private void parse(JavaClass classFile)
        throws ClassParseException
        {
        parseClassName(classFile);
        parseFlags(classFile);
        parseExtends(classFile);
        parseImplements(classFile);
        parseReferences(classFile);
        }
    
    private void parseClassName(JavaClass classFile)
        { className = classFile.getClassName(); }
    
    public String getClassName()
        { return className; }
    
    public boolean isComplete()
        { return true; }
    
    private void parseFlags(JavaClass classFile)
        throws ClassParseException
        {
        isInterface = classFile.isInterface();
        isAbstract  = classFile.isAbstract();
        isFinal     = classFile.isFinal();
        accessModifier = translateAccess(classFile);
        }
        
    public boolean isInterface() { return isInterface; }
    public boolean isAbstract()  { return isAbstract; }
    public boolean isFinal()     { return isFinal; }

    public AccessModifier getAccessModifier()
        { return accessModifier; }

    private void parseExtends(JavaClass classFile)
        throws ClassParseException
        { extendsName = classFile.getSuperclassName(); }
    
    public String getExtends()
        { return extendsName; }
    
    private void parseImplements(JavaClass classFile)
        throws ClassParseException
        {
        implementsNames = Collections.unmodifiableSet(
            new TreeSet(
                Arrays.asList(
                    classFile.getInterfaceNames())));
        }
    
    public Set/*<String>*/ getImplements()
        { return implementsNames; }
    
    private void parseReferences(JavaClass classFile)
        throws ClassParseException
        {
        references = new CompositeMultiMap(TreeMap.class, HashSet.class);
        parseConstantPoolReferences(classFile);
        parseMethodReferences(classFile);
        parseFieldReferences(classFile);
        references = InnigCollections.unmodifiableMultiMap(references);
        }
    
    private void parseConstantPoolReferences(JavaClass classFile)
        throws ClassParseException
        {
        // Add accessed classes from constant pool entries
        ConstantPool constantPool = classFile.getConstantPool();
        Constant[] constants = constantPool.getConstantPool();
        for(int a = 1; a < constants.length; a++)
            if(constants[a] instanceof ConstantClass)
                addReference(
                    new Reference(
                        getClassName(),
                        constantPool.constantToString(constants[a]),
                        ReferenceType.CONSTANT_POOL,
                        null,
                        null));
        }
    
    private void parseMethodReferences(JavaClass classFile)
        throws ClassParseException
        {
        // Add yet more accessed classes from method & field signatures
        Method[] methods = classFile.getMethods();
        for(int m = 0; m < methods.length; m++)
            {
            Method method = methods[m];
            AccessModifier methodAccess = translateAccess(method);
            
            List paramsAndReturn = 
                ClassNameTranslator.signatureToClassNames(
                    method.getSignature());
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
            
            if(method.getExceptionTable() != null)
                {
                String[] exceptionNames = method.getExceptionTable().getExceptionNames();
                for(int e = 0; e < exceptionNames.length; e++)
                    addReference(
                        new Reference(
                            getClassName(),
                            exceptionNames[e],
                            ReferenceType.METHOD_THROWS,
                            method.getName(),
                            methodAccess));
                }
            }
        }
    
    private void parseFieldReferences(JavaClass classFile)
        throws ClassParseException
        {
        Field[] fields = classFile.getFields();
        for(int a = 0; a < fields.length; a++)
            {
            Field field = fields[a];
            List types =
                ClassNameTranslator.signatureToClassNames(
                    field.getSignature());
            if(types.size() != 1)
                throw new ClassParseException(
                    "expected one type for field " + className + '.' + field.getName()
                    + "; got: " + types);

            addReference(
                new Reference(
                    getClassName(),
                    (String) types.get(0),
                    ReferenceType.FIELD_API,
                    field.getName(),
                    translateAccess(field)));
            }
        }
        
    private AccessModifier translateAccess(AccessFlags accessFlags)
        throws ClassParseException
        {
        if(accessFlags.isPublic())
            return AccessModifier.PUBLIC;
        else if(accessFlags.isProtected())
            return AccessModifier.PROTECTED;
        else if(accessFlags.isPrivate())
            return AccessModifier.PRIVATE;
        else
            return AccessModifier.PACKAGE;
        }
    
    private void addReference(Reference ref)
        { references.put(ref.getTo(), ref); }
    
    public MultiMap/*<String,Reference>*/ getReferences()
        { return references; }
    
    public String toString()
        { return getClassName(); }
    
    private String className, extendsName;
    private boolean isInterface, isAbstract, isFinal;
    private AccessModifier accessModifier;
    private Set/*<String>*/ implementsNames;
    private MultiMap/*<String,Reference>*/ references;
    }

