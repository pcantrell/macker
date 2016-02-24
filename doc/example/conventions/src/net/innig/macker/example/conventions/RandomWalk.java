package net.innig.macker.example.conventions;

import java.util.*;

public final class RandomWalk
    {
    public static void walk(Tree tree, int hops)
        {
        while(hops-- > 0)
            {
            System.out.println("hop: " + tree);
            if(tree.getParent() != null && rand.nextInt(4) == 0)
                tree = tree.getParent();
            else
                {
                List children = new ArrayList(tree.getChildren());
                if(children.isEmpty())
                    tree = tree.getParent();
                else
                    tree = (Tree) children.get(rand.nextInt(children.size()));
                }
            if(tree == null)
                return;
            }
        }
    
    private static Random rand = new Random();
    
    private RandomWalk() { } // In the future, Macker will be able to require this, too
    }