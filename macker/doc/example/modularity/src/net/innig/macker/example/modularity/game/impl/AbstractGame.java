package net.innig.macker.example.modularity.game.impl;

import net.innig.macker.example.modularity.game.*;
import net.innig.macker.example.modularity.player.*;
import java.util.*;

public abstract class AbstractGame
    implements Game
    {
    public AbstractGame()
        {
        player = new Player[2];
        score = new int[2];
        prevMove = new Move[2];
        }
    
    public void setPlayer(int playerNum, Player player)
        { this.player[playerNum] = player; }
    
    public Player getPlayer(int playerNum)
        { return player[playerNum]; }
    
    public int getScore(int playerNum)
        { return score[playerNum]; }
    
    public Move getPreviousMove(int playerNum)
        { return prevMove[playerNum]; }
    
    public void move()
        {
        Move move0 = player[0].nextMove(getLegalMoves(), prevMove[1]);
        Move move1 = player[1].nextMove(getLegalMoves(), prevMove[0]);
        prevMove[0] = move0;
        prevMove[1] = move1;
        score[0] += move0.getScoreFor(move1);
        score[1] += move1.getScoreFor(move0);
        }

    private Player[] player;
    private int[] score;
    private Move[] prevMove;
    }
