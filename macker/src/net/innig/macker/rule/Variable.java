package net.innig.macker.rule;

import net.innig.macker.structure.ClassManager;
import net.innig.macker.structure.ClassInfo;
import net.innig.macker.event.MackerIsMadException;

import java.util.*;

public class Variable
    extends Rule
    {
    public Variable(String name, String value)
        {
        setVariableName(name);
        setValue(value);
        }
        
    public String getVariableName()
        { return variableName; }
    
    public void setVariableName(String variableName)
        { this.variableName = variableName; }
    
    public String getValue()
        { return value; }
    
    public void setValue(String value)
        { this.value = value;}

    public void check(
            EvaluationContext context,
            ClassManager classes)
        throws RulesException, MackerIsMadException
        {
        context.setVariableValue(getVariableName(), getValue());
        }
    
    private String variableName, value;
    }