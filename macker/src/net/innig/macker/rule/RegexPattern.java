package net.innig.macker.rule;

public class RegexPattern
    extends Pattern
    {
    public RegexPattern(String regexStr)
        { setMatchString(regexStr); }
        
    public String getMatchString()
        { return regexStr; }

    public void setMatchString(String regexStr)
        { this.regexStr = regexStr; }
    
    public String toString()
        { return '"' + regexStr + '"'; }
    
    private String regexStr;
    }