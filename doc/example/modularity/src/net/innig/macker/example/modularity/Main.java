package net.innig.macker.example.modularity;

import net.innig.macker.example.modularity.game.*;
import net.innig.macker.example.modularity.player.*;
import java.util.*;

public class Main
    {
    public static void main(String[] args)
        {
        Game g = GameFactory.createGame(args[0]);
        g.setPlayer(0, PlayerFactory.createPlayer(args[1]));
        g.setPlayer(1, PlayerFactory.createPlayer(args[2]));
        
        while(true)
            {
            System.out.println(
                g.getPreviousMove(0) + " / " + g.getPreviousMove(1)
                + " (score: " + g.getScore(0) + " to " + g.getScore(1) + ")");
            g.move();
            }
        }
    }
