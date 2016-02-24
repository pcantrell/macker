package net.innig.macker.example.modularity.game.impl;

import net.innig.macker.example.modularity.game.*;
import net.innig.macker.example.modularity.player.*;
import java.util.*;

public class RochambeauGame
    extends AbstractGame
    {
    public Set getLegalMoves()
        { return RochambeauMove.ALL; }
    }
