package net.innig.macker.example.modularity.game;

import net.innig.macker.example.modularity.game.impl.*;
import java.util.*;

public class GameFactory
    {
    public static Set getGameNames()
        {
        Set games = new TreeSet();
        games.add("(R)ochambeau");
        games.add("(P)risoner's Dilemma");
        return Collections.unmodifiableSet(games);
        }
    
    public static Game createGame(String name)
        {
        name = name.trim().toLowerCase();
        if("rochambeau".startsWith(name))
            return new RochambeauGame();
        if("prisoner's dilemma".startsWith(name))
            return new PrisonersDilemmaGame();
        throw new IllegalArgumentException("Unknown game: " + name);
        }
    }
