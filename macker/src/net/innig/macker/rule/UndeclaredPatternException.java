package net.innig.macker.rule;

public class UndeclaredPatternException
    extends RulesException
    {
    public UndeclaredPatternException(String patternName)
        { super("Pattern named \"" + patternName + "\" not declared"); }
    }