package net.innig.macker.example.modularity.game;

import net.innig.macker.example.modularity.player.*;
import java.util.*;

/**
    A very simple two-player game in normal form.
*/
public interface Game
    {
    public Set getLegalMoves();
    
    public void setPlayer(int playerNum, Player player);
    public Player getPlayer(int playerNum);
    
    public int getScore(int playerNum);
    public Move getPreviousMove(int playerNum);
    public void move();
    }
