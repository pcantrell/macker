package net.innig.macker.structure;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;

import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.CPInfo;
import org.gjt.jclasslib.structures.constants.ConstantClassInfo;
import org.gjt.jclasslib.io.ClassFileReader;
import org.gjt.jclasslib.structures.InvalidByteCodeException;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

public class ClassInfo
    {
    public ClassInfo(File classFile)
        throws IOException, InvalidByteCodeException
        { this(ClassFileReader.readFromFile(classFile)); }
    
    public ClassInfo(InputStream classFileStream)
        throws IOException, InvalidByteCodeException
        { this(ClassFileReader.readFromInputStream(classFileStream)); }
    
    private ClassInfo(ClassFile classFile)
        throws InvalidByteCodeException
        {
        CPInfo[] constantPool = classFile.getConstantPool();
        className =
            slashesToDots(
                ((ConstantClassInfo) constantPool[classFile.getThisClass()]).getName())
            .intern();
        references = new TreeSet();
        for(int n = 1; n < constantPool.length; n++)
            if(constantPool[n] instanceof ConstantClassInfo)
                references.add(
                    slashesToDots(
                        ((ConstantClassInfo) constantPool[n]).getName())
                    .intern());
        references = Collections.unmodifiableSet(references);
        }
    
    public String getClassName()
        { return className; }
    
    public Set/*<String>*/ getReferences()
        { return references; }
    
    public String toString()
        { return getClassName(); }
    
    static private String slashesToDots(String className)
        {
        if(separatorRE == null)
            try { separatorRE = new RE("/|\\$"); }
            catch(RESyntaxException rese)
                { throw new RuntimeException("Can't initialize ClassInfo.separatorRE: " + rese); } 
        return separatorRE.subst(className, ".");
        }
    
    static private RE separatorRE;
    
    public String className;
    private Set/*<String>*/ references;
    }



