package net.innig.macker.example.modularity.game.impl;

import net.innig.macker.example.modularity.game.*;
import net.innig.util.EnumeratedType;
import java.util.*;

public final class RochambeauMove
    extends EnumeratedType
    implements Move
    {
    static final Move
        SCISSORS = new RochambeauMove("scissors", 0),
        PAPER    = new RochambeauMove("paper", 1),
        STONE    = new RochambeauMove("stone", 2);
    
    static final Set ALL = Collections.unmodifiableSet(new HashSet(Arrays.asList(
        new Move[] { SCISSORS, PAPER, STONE } )));
    
    private RochambeauMove(String name, int num)
        {
        super(name);
        this.num = num;
        }

    public int getScoreFor(Move other)
        { return ((num + 1) % 3 == ((RochambeauMove) other).num) ? 1 : 0; }

    private int num;
    }
