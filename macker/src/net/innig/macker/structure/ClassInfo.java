package net.innig.macker.structure;

import java.util.Set;

public abstract class ClassInfo
    {
    public abstract String getClassName();
    
    public abstract Set/*<String>*/ getReferences();
    }



