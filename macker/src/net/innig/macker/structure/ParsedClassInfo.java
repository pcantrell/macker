/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002-2003 Paul Cantrell
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

/**
    Class info retrieved from a class file.
*/
public class ParsedClassInfo
    extends AbstractClassInfo
    {
    ParsedClassInfo(ClassManager classManager, File classFile)
        throws IOException, ClassParseException
        {
        super(classManager);
        try { parse(new ClassParser(classFile.getPath()).parse()); }
        catch(ClassFormatError cfe)
            { throw new ClassParseException(cfe); }
        }
    
    ParsedClassInfo(ClassManager classManager, InputStream classFileStream)
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
        { fullClassName = classFile.getClassName(); }
    
    public String getFullName()
        { return fullClassName; }
    
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
        { extendsClass = getSafeClassInfo(classFile.getSuperclassName()); }
    
    public ClassInfo getExtends()
        { return extendsClass; }
    
    private void parseImplements(JavaClass classFile)
        throws ClassParseException
        {
        implementsClasses = new TreeSet();
        String[] names = classFile.getInterfaceNames();
        for(int n = 0; n < names.length; n++)
            implementsClasses.add(getSafeClassInfo(names[n]));
        implementsClasses = Collections.unmodifiableSet(implementsClasses);
        }
    
    public Set/*<ClassInfo>*/ getImplements()
        { return implementsClasses; }
    
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
                        this,
                        getSafeClassInfo(
                            constantPool.constantToString(constants[a])),
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
                    "unable to read types for method " + fullClassName + '.' + method.getName());
            
            for(Iterator i = paramsAndReturn.iterator(); i.hasNext(); )
                {
                String refTo = (String) i.next();
                addReference(
                    new Reference(
                        this,
                        getSafeClassInfo(refTo, method.getSignature()),
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
                            this,
                            getSafeClassInfo(exceptionNames[e]),
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
                    "expected one type for field " + fullClassName + '.' + field.getName()
                    + "; got: " + types + " (signature is \"" + field.getSignature() + '"');

            addReference(
                new Reference(
                    this,
                    getSafeClassInfo((String) types.get(0), field.getSignature()),
                    ReferenceType.FIELD_SIGNATURE,
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
    
    private ClassInfo getSafeClassInfo(String className)
        throws ClassParseException
        { return getSafeClassInfo(ClassNameTranslator.typeConstantToClassName(className), className); }
    
    private ClassInfo getSafeClassInfo(String className, String unparsedClassName)
        throws ClassParseException
        {
        if(!ClassNameTranslator.isJavaIdentifier(className))
            throw new ClassParseException("unable to parse class name / signature: \"" + unparsedClassName + "\" (got \"" + className + "\")");
        return getClassManager().getClassInfo(className);
        }
    
    private void addReference(Reference ref)
        { references.put(ref.getTo(), ref); }
    
    public MultiMap/*<ClassInfo,Reference>*/ getReferences()
        { return references; }
    
    private String fullClassName;
    private boolean isInterface, isAbstract, isFinal;
    private AccessModifier accessModifier;
    private ClassInfo extendsClass;
    private Set/*<ClassInfo>*/ implementsClasses;
    private MultiMap/*<ClassInfo,Reference>*/ references;
    }

