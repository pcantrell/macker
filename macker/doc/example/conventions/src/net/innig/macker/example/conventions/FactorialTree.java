package net.innig.macker.example.conventions;

import java.util.*;
import java.io.*;

public class FactorialTree
    extends StringTree
    {
    public FactorialTree()
        {
        super(null, "");
        n = 1;
        }
    
    public FactorialTree(FactorialTree parent, String word, int n)
        {
        super(parent, word);
        this.n = n;
        }

    protected Set/*<String>*/ getChildSuffixes()
        {
        Set suffixes = new TreeSet();
        for(int j = n; j > 0; j--)
            suffixes.add('.' + String.valueOf(j));
        return suffixes;
        }
    
    protected StringTree makeChild(String childWord)
        { return new FactorialTree(this, childWord, n+1); }
    
    private int n;
    }