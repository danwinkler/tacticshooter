package com.danwink.tacticshooter.ai;

import java.util.ArrayList;
import java.util.HashMap;

import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.ComputerPlayer;
import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Unit.UnitType;
import com.danwink.tacticshooter.network.Message;
import com.phyloa.dlib.util.DMath;

public class Good1 extends ComputerPlayer 
{
	HashMap<String, Unit> tagged = new HashMap<String, Unit>();
	
	public void update( PathFinder finder ) 
	{
		//SCOUT
		Unit scout = tagged.get( "scout" );
		if( scout == null || !scout.alive )
		{
			scout = findScout();
			if( scout != null )
			{
				tagged.remove( "scout" );
				tagged.put( "scout", scout );
			}
		}
		if( scout != null )
		{
			setScoutTarget( scout, finder );
		}
		
		if( player.money > 20 )
		{
			ci.sl.received( fc, new Message( MessageType.BUILDUNIT, UnitType.values()[DMath.randomi(0, UnitType.values().length)] ) );
		}
	}
	
	
	public void setScoutTarget( Unit u, PathFinder finder )
	{
		ArrayList<Building> checked = new ArrayList<Building>();
		
		while( checked.size() < l.buildings.size() )
		{
			Building closest = null;
			for( int i = 0; i < l.buildings.size(); i++ )
			{
				
			}
		}
	}
	
	public Unit findScout()
	{
		for( Unit u : units )
		{
			if( u.alive && (u.type == UnitType.LIGHT || u.type == UnitType.SCOUT) )
			{
				return u;
			}
		}
		return null;
	}
}