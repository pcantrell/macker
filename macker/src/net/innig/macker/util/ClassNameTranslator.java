package net.innig.macker.util;

import java.util.*;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

public class ClassNameTranslator
    {
    static public String typeConstantToClassName(String typeName)
        {
        if(arrayExtractorRE.match(typeName))
            {
            if(arrayExtractorRE.getParen(2) != null)
                return (String) primitiveTypeMap.get(arrayExtractorRE.getParen(2));
            if(arrayExtractorRE.getParen(3) != null)
                return resourceToClassName(arrayExtractorRE.getParen(3));
            }
        return resourceToClassName(typeName);
        }
    
    static public String resourceToClassName(String className)
        { return slashRE.subst(classSuffixRE.subst(className, ""), ".").intern(); }
    
    static public String classToResourceName(String resourceName)
        { return (dotRE.subst(resourceName, "/") + ".class").intern(); }
    
    static private RE classSuffixRE, slashRE, dotRE, arrayExtractorRE;
    static private Map/*<String,String>*/ primitiveTypeMap;
    static
        {
        try {
            classSuffixRE = new RE("\\.class$");
            slashRE = new RE("/");
            dotRE = new RE("\\.");
            arrayExtractorRE = new RE("^(\\[+([BSIJCFDZ])|\\[+L(.*);)$");
            }
        catch(RESyntaxException rese)
            { throw new RuntimeException("Can't initialize ClassNameTranslator: " + rese); } 
        
        primitiveTypeMap = new HashMap();
        primitiveTypeMap.put("B", "byte");
        primitiveTypeMap.put("S", "short");
        primitiveTypeMap.put("I", "int");
        primitiveTypeMap.put("J", "long");
        primitiveTypeMap.put("C", "char");
        primitiveTypeMap.put("F", "float");
        primitiveTypeMap.put("D", "double");
        primitiveTypeMap.put("Z", "boolean");
        }
    }
