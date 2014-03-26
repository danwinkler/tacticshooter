package com.danwink.tacticshooter;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Player;
import com.danwink.tacticshooter.gameobjects.Team;
import com.phyloa.dlib.util.DFile;

public class JSAPI
{
	ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine engine = mgr.getEngineByName("JavaScript");
	
    public TacticServer ts;
	
    public JSAPI( TacticServer ts )
    {
    	this.ts = ts;
    }
    
	public void tick( int frame )
	{
		try 
		{
			Bindings bindings = engine.getBindings( ScriptContext.ENGINE_SCOPE );
		    
			Object[] c = ts.players.keySet().toArray();
			
			bindings.put( "api", this );
			
			/*Set<Entry<Integer, Player>> playerSet = ts.players.entrySet();
			int[] players = new int[playerSet.size()];
		    Iterator<Entry<Integer, Player>> pi = playerSet.iterator();
			for( int i = 0; pi.hasNext(); i++ )
		    {
				Entry<Integer, Player> e = pi.next();
		    	players[i] = e.getValue().id;
		    }
		    */
		    bindings.put( "players", ts.players.keySet().toArray() );
			
		    int[] buildings = new int[ts.l.buildings.size()];
		    for( int i = 0; i < ts.l.buildings.size(); i++ )
		    {
		    	buildings[i] = ts.l.buildings.get( i ).id;
		    }
		    bindings.put( "buildings", buildings );
		    
		    bindings.put( "out", System.out );
		    
			engine.eval( "tick( " + frame + " );" );
		} catch( ScriptException e ) {
			e.printStackTrace();
		}
	}
	
	public void load( String path )
	{
		try 
		{
			engine.eval( DFile.loadText( path ) );
		} 
		catch( FileNotFoundException | ScriptException e ) 
		{
			e.printStackTrace();
		}
	}
	
	public int getPlayerMoney( int id )
	{
		return ts.players.get( id ).money;
	}
	
	public void setPlayerMoney( int id, int money )
	{
		ts.players.get( id ).money = money;
		ts.updatePlayer( id );
	}
	
	public void addPlayerMoney( int id, int money )
	{
		ts.players.get( id ).money += money;
		ts.updatePlayer( id );
	}
	
	public int getPlayerTeam( int id )
	{
		Team t = ts.players.get( id ).team;
		return t == null ? -1 : t.id;
	}
	
	public int getBuildingTeam( int id )
	{
		for( Building b : ts.l.buildings )
		{
			if( b.id == id )
			{
				return b.t != null ? b.t.id : -1;
			}
		}
		return -1;
	}
}
