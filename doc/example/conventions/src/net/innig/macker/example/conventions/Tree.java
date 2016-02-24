package net.innig.macker.example.conventions;

import java.util.*;

public interface Tree
    {
    public Tree getParent();
    public HashSet/*<Tree>*/ getChildren(); // WRONG: should return Set
    }