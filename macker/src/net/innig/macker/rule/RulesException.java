package net.innig.macker.rule;

/**
    Indicates an illegal or ill-formed rule.
*/

public abstract class RulesException
    extends Exception
    {
    public RulesException(String message)
        { super(message); }
    }