package net.innig.macker.util;

import net.innig.macker.rule.RulesException;

public interface IncludeExcludeNode
    {
    public boolean isInclude();

    public boolean matches()
        throws RulesException;
    
    public IncludeExcludeNode getChild();
    
    public IncludeExcludeNode getNext();
    }