package net.innig.macker.rule;

import org.jdom.Element;

import org.apache.regexp.RESyntaxException;

public class RegexPatternSyntaxException
    extends RulesException
    {
    public RegexPatternSyntaxException(String regexp)
        { this(regexp, null); }
    
    public RegexPatternSyntaxException(String regexp, RESyntaxException root)
        {
        super("\"" + regexp + "\" is not a valid Macker regexp pattern"
            + (root == null ? "" : ": " + root.toString()));
        this.regexp = regexp;
        root.printStackTrace(System.err);
        }
    
    public final String getRegexp()
        { return regexp; }
    
    private final String regexp;
    }