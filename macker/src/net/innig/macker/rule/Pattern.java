package net.innig.macker.rule;

public abstract class Pattern
    {
    public static final Pattern ALL =
        new Pattern()
            {
            // public boolean matches(...) { return true; }
            public String toString() { return "Pattern.ALL"; }
            };
    
//    public abstract boolean matches(EvaluationContext context, String className);
    }