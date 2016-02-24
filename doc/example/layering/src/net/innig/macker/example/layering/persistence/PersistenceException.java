package net.innig.macker.example.layering.persistence;

public class PersistenceException
    extends Exception
    {
    public PersistenceException(String message)
        { super(message); }

    public PersistenceException(String message, Exception e)
        { super(message + ": " + e.toString()); }
    }