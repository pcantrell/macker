package net.innig.macker.structure;

import java.util.*;

public class PrimitiveTypeInfo
    extends ClassInfo
    {
    static public PrimitiveTypeInfo getPrimitiveTypeInfo(String typeName)
        { return (PrimitiveTypeInfo) nameToTypeMap.get(typeName); }

    static private Map nameToTypeMap;
    static
        {
        nameToTypeMap = new HashMap();
        nameToTypeMap.put("byte",    new PrimitiveTypeInfo("byte"));
        nameToTypeMap.put("short",   new PrimitiveTypeInfo("short"));
        nameToTypeMap.put("int",     new PrimitiveTypeInfo("int"));
        nameToTypeMap.put("long",    new PrimitiveTypeInfo("long"));
        nameToTypeMap.put("char",    new PrimitiveTypeInfo("char"));
        nameToTypeMap.put("boolean", new PrimitiveTypeInfo("boolean"));
        nameToTypeMap.put("float",   new PrimitiveTypeInfo("float"));
        nameToTypeMap.put("double",  new PrimitiveTypeInfo("double"));
        }
    
    private PrimitiveTypeInfo(String className)
        { this.className = className; }
    
    public String getClassName()
        { return className; }
    
    public Set/*<String>*/ getReferences()
        { return Collections.EMPTY_SET; }
    
    public String toString()
        { return getClassName(); }
    
    public String className;
    }



