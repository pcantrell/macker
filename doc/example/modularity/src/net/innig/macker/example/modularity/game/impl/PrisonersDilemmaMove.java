package net.innig.macker.example.modularity.game.impl;

import net.innig.macker.example.modularity.game.*;
import net.innig.util.EnumeratedType;
import java.util.*;

public final class PrisonersDilemmaMove
    extends EnumeratedType
    implements Move
    {
    static final Move
        COOPERATE = new PrisonersDilemmaMove("cooperate"),
        DEFECT    = new PrisonersDilemmaMove("defect");
    
    static final Set ALL = Collections.unmodifiableSet(new HashSet(Arrays.asList(
        new Move[] { COOPERATE, DEFECT } )));
    
    private PrisonersDilemmaMove(String name)
        { super(name); }

    public int getScoreFor(Move other)
        {
        if(this == COOPERATE && other == COOPERATE)
            return 5;
        if(this == DEFECT && other == COOPERATE)
            return 10;
        if(this == COOPERATE && other == DEFECT)
            return 0;
        if(this == DEFECT && other == DEFECT)
            return 2;
        throw new Error("unknown move");
        }

    private int num;
    }
