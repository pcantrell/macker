package net.innig.macker.example.layering.model;

import net.innig.macker.example.layering.persistence.*;

import java.util.*;

public class Thinger
    {
    private static ThingerPersistence getPersist()
        throws PersistenceException
        {
        if(persist == null)
            persist = new ThingerPersistence();
        return persist;
        }
    private static ThingerPersistence persist;

    public static Set getAll()
        throws PersistenceException
        {
        Set allNames = getPersist().selectAll();
        Set allThingers = new HashSet();
        for(Iterator i = allNames.iterator(); i.hasNext(); )
            allThingers.add(new Thinger((String) i.next(), true));
        return allThingers;
        }
    
    public Thinger(String name)
        { this(name, false); }
    
    private Thinger(String name, boolean stored)
        {
        this.name = name;
        this.stored = stored;
        }
    
    public String getName()
        { return name; }
    
    public void store()
        throws PersistenceException
        {
        if(!stored)
            getPersist().insert(name);
        stored = true;
        }
    
    public void delete()
        throws PersistenceException
        {
        getPersist().delete(name);
        stored = false;
        }
    
    public String toString()
        { return name; }
    
    private boolean stored = false;
    private final String name;
    }