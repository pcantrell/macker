package net.innig.macker.rule;

import org.jdom.Element;

public class RegexPatternSyntaxException
    extends RulesException
    {
    public RegexPatternSyntaxException(String regexp)
        {
        super("\"" + regexp + "\" is not a valid Macker regexp pattern");
        this.regexp = regexp;
        }
    
    public final String getRegexp()
        { return regexp; }
    
    private final String regexp;
    }