package net.innig.macker.example.modularity.player.impl;

import net.innig.macker.example.modularity.player.*;
import net.innig.macker.example.modularity.game.*;
import java.util.*;

public class TitForTatPlayer
    implements Player
    {
    public Move nextMove(Set legalMoves, Move otherPreviousMove)
        {
        return (otherPreviousMove != null)
            ? otherPreviousMove
            : fallback.nextMove(legalMoves, otherPreviousMove);
        }
    
    private RandomPlayer fallback = new RandomPlayer();
    }