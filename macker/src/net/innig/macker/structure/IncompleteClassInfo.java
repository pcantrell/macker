package net.innig.macker.structure;

import java.util.Set;

public class IncompleteClassInfo
    extends ClassInfo
    {
    public IncompleteClassInfo(String className)
        { this.className = className; }
    
    public String getClassName()
        { return className; }
    
    public Set/*<String>*/ getReferences()
        {
        throw new UnsupportedOperationException(
            "Unable to check references for class " + className
            + ", because the class file could not be loaded."
            + " Make sure it is in Macker's classpath.");
        }
    
    public String toString()
        { return getClassName(); }
    
    public String className;
    }



