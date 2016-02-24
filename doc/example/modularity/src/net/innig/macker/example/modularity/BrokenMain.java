package net.innig.macker.example.modularity;

import net.innig.macker.example.modularity.game.*;
import net.innig.macker.example.modularity.game.impl.*;
import net.innig.macker.example.modularity.player.*;
import net.innig.macker.example.modularity.player.impl.*;
import java.util.*;

public class BrokenMain
    {
    public static void main(String[] args)
        {
        Game g = new RochambeauGame();
        g.setPlayer(0, new RandomPlayer());
        g.setPlayer(1, new CyclicPlayer());
        
        while(true)
            {
            System.out.println(
                g.getPreviousMove(0) + " / " + g.getPreviousMove(1)
                + " (score: " + g.getScore(0) + " to " + g.getScore(1) + ")");
            g.move();
            }
        }
    }
