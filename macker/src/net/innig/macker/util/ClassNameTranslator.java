package net.innig.macker.util;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

public class ClassNameTranslator
    {
    static public String resourceToClassName(String className)
        { return slashRE.subst(classSuffixRE.subst(className, ""), ".").intern(); }
    
    static public String classToResourceName(String resourceName)
        { return (dotRE.subst(resourceName, "/") + ".class").intern(); }
    
    static private RE classSuffixRE, slashRE, dotRE;
    static
        {
        try {
            classSuffixRE = new RE("\\.class$");
            slashRE = new RE("/");
            dotRE = new RE("\\.");
            }
        catch(RESyntaxException rese)
            { throw new RuntimeException("Can't initialize ClassNameTranslator: " + rese); } 
        }
    }



