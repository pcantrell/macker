package net.innig.macker.rule;

import net.innig.macker.structure.ClassManager;
import net.innig.macker.event.MackerIsMadException;

public abstract class Rule
    {
    public abstract void check(
            EvaluationContext context,
            ClassManager classes)
        throws RulesException, MackerIsMadException;
    }