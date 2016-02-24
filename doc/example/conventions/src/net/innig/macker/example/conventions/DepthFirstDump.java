package net.innig.macker.example.conventions;

import java.util.*;

public class DepthFirstDump
    {
    public static void dump(Tree tree, int levels, int maxChildren, String prefix)
        {
        if(levels == 0)
            {
            System.out.println(prefix + tree + " ...");
            return;
            }
        
        System.out.println(prefix + tree);
        prefix = prefix + "   ";
        int childCount = 0;
        for(Iterator childIter = tree.getChildren().iterator(); childIter.hasNext(); )
            {
            childCount++;
            if(childCount > maxChildren)
                {
                System.out.println(prefix + "...");
                break;
                }
            
            Tree child = (Tree) childIter.next();
            dump(child, levels-1, maxChildren, prefix);
            }
        }
    
    private DepthFirstDump() { }
    }