package net.innig.macker.rule;

public class UndeclaredVariableException
    extends RulesException
    {
    public UndeclaredVariableException(String variableName)
        { super("Variable named \"" + variableName + "\" not declared"); }
    }