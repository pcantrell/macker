package net.innig.macker.example.modularity.player;

import net.innig.macker.example.modularity.game.*;
import java.util.*;

public interface Player
    {
    public Move nextMove(Set legalMoves, Move otherPreviousMove);
    }