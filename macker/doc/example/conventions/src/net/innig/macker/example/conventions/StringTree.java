package net.innig.macker.example.conventions;

import java.util.*;
import java.io.*;

public abstract class StringTree
    implements Tree
    {
    public StringTree(StringTree parent, String word)
        {
        this.parent = parent;
        this.word = word;
        }
    
    public Tree getParent()
        { return parent; }
    
    public HashSet/*<Tree>*/ getChildren()
        {
        HashSet children = new HashSet();
        StringBuffer newWord = new StringBuffer(word);
        for(Iterator i = getChildSuffixes().iterator(); i.hasNext();)
            {
            String suffix = (String) i.next();
            newWord.setLength(word.length());
            newWord.append(suffix);
            children.add(makeChild(newWord.toString()));
            }
        return children;
        }
    
    protected abstract Set/*<String>*/ getChildSuffixes();
    
    protected abstract StringTree makeChild(String childWord);
        
    public String toString()
        { return quote(word); }
    
    static String quote(String word)
        { return '"' + word + '"'; }
    
    private StringTree parent;
    private String word;
    }