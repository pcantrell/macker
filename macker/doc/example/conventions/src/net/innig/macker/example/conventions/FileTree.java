package net.innig.macker.example.conventions;

import java.util.*;
import java.io.*;

public class FileTree
    implements Tree
    {
    public FileTree(File file)
        { this.file = file; }
    
    public Tree getParent()
        {
        File parentFile = file.getParentFile();
        return (parentFile == null) ? null : new FileTree(parentFile);
        }
    
    public HashSet getChildren()
        {
        HashSet children = new HashSet();
        File[] childFiles = file.listFiles();
        if(childFiles != null)
            for(int f = 0; f < childFiles.length; f++)
                children.add(new FileTree(childFiles[f]));
        return children;
        }
    
    public String toString()
        { return StringTree.quote(file.getPath()); }
    
    private File file;
    }