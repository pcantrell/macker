package net.innig.macker.rule;

import org.jdom.Element;

import org.apache.regexp.RESyntaxException;

public class RegexPatternSyntaxException
    extends RulesException
    {
    public RegexPatternSyntaxException(String regexp)
        { this(regexp, ""); }
    
    public RegexPatternSyntaxException(String regexp, RESyntaxException root)
        {
        this(regexp, root.toString());
        root.printStackTrace(System.out);
        }
    
    public RegexPatternSyntaxException(String regexp, String message)
        {
        super("\"" + regexp + "\" is not a valid Macker regexp pattern" + message);
        this.regexp = regexp;
        }
    
    public final String getRegexp()
        { return regexp; }
    
    private final String regexp;
    }