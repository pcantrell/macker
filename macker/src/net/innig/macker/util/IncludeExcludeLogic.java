package net.innig.macker.util;

import net.innig.macker.rule.RulesException;

public class IncludeExcludeLogic
    {
    public static boolean apply(IncludeExcludeNode node)
        throws RulesException
        {
        return applyNext(
            node,
            node.isInclude()
                ? false  // include starts with all excluded, and
                : true); // exclude starts with all included
        }

    private static boolean applyNext(
            IncludeExcludeNode node,
            boolean prevMatches)
        throws RulesException
        {
        IncludeExcludeNode child = node.getChild(), next = node.getNext();
        boolean curMatches = node.matches();
        boolean matchesSoFar =
            node.isInclude()
                ? prevMatches || ( curMatches && (child == null || apply(child)))
                : prevMatches && (!curMatches || (child != null && apply(child)));
        return
            (next == null)
                ? matchesSoFar
                : applyNext(next, matchesSoFar);
        }
    }