package net.innig.macker.rule;

import net.innig.macker.structure.ClassInfo;

public abstract class Pattern
    {
    public static final Pattern ALL =
        new Pattern()
            {
            public boolean matches(EvaluationContext context, ClassInfo classInfo)
                { return true; }
            public String toString() { return "Pattern.ALL"; }
            };
    
    public abstract boolean matches(EvaluationContext context, ClassInfo classInfo)
        throws RulesException;
    }