package net.innig.macker.example.modularity.player;

import net.innig.macker.example.modularity.player.impl.*;
import java.util.*;

public class PlayerFactory
    {
    public static Set getPlayerNames()
        {
        Set games = new TreeSet();
        games.add("(R)andom");
        games.add("(T)it for Tat");
        return Collections.unmodifiableSet(games);
        }
    
    public static Player createPlayer(String name)
        {
        name = name.trim().toLowerCase();
        if("random".startsWith(name))
            return new RandomPlayer();
        if("tit for tat".startsWith(name))
            return new TitForTatPlayer();
        if("cyclic".startsWith(name))
            return new CyclicPlayer();
        throw new IllegalArgumentException("Unknown player: " + name);
        }
    }
