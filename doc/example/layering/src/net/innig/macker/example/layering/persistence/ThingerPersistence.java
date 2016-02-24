package net.innig.macker.example.layering.persistence;

import java.sql.*;
import java.util.*;

public class ThingerPersistence
    {
    public ThingerPersistence()
        throws PersistenceException
        {
        Properties connProps = new Properties();
        try
            { connProps.load(getClass().getClassLoader().getResourceAsStream("db.properties")); }
        catch(Exception e)
            { throw new PersistenceException("Unable to load db.properties", e); }
        
        try
            { Class.forName(connProps.getProperty("db.driver")); }
        catch(ClassNotFoundException cnfe)
            { throw new PersistenceException("Unable to load DB driver", cnfe); }
        
        try
            {
            conn = DriverManager.getConnection(
                connProps.getProperty("db.url"),
                connProps.getProperty("db.user"),
                connProps.getProperty("db.pass"));
            conn.setAutoCommit(false);
            }
        catch(SQLException sqle)
            { throw new PersistenceException("Unable to connect to database", sqle); }
            
        try
            {
            selStmt = conn.prepareStatement("select name from thinger");
            insStmt = conn.prepareStatement("insert into thinger (name) values (?)");
            delStmt = conn.prepareStatement("delete from thinger where name = ?");
            }
        catch(SQLException sqle)
            { throw new PersistenceException("Unable to prepare statements", sqle); }
        }
    
    public Set selectAll()
        throws PersistenceException
        {
        Set thingers = new HashSet();
        try
            {
            ResultSet rs = selStmt.executeQuery();
            while(rs.next())
                thingers.add(rs.getString(1));
            rs.close();
            }
        catch(SQLException sqle)
            { throw new PersistenceException("Unable to select thingers", sqle); }
        return thingers;
        }
    
    public void insert(String name)
        throws PersistenceException
        {
        boolean done = false;
        try
            {
            insStmt.clearParameters();
            insStmt.setString(1, name);
            insStmt.execute();
            conn.commit();
            done = true;
            }
        catch(SQLException sqle)
            { throw new PersistenceException("Unable to insert thinger", sqle); }
        finally
            { quietRollback(done); }
        }
    
    public void delete(String name)
        throws PersistenceException
        {
        boolean done = false;
        try
            {
            if(delStmt.executeUpdate(name) == 0)
                throw new PersistenceException("No such thinger: \"" + name + '"');
            conn.commit();
            done = true;
            }
        catch(SQLException sqle)
            { throw new PersistenceException("Unable to delete thinger", sqle); }
        finally
            { quietRollback(done); }
        }
    
    private void quietRollback(boolean doit)
        {
        if(doit)
            try { conn.rollback(); }
            catch(Exception e) { }
        }
    
    private Connection conn;
    private PreparedStatement selStmt, insStmt, delStmt;
    }
