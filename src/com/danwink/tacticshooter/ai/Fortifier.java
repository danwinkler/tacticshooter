package com.danwink.tacticshooter.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.phyloa.dlib.math.Point2i;

import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.ComputerPlayer;
import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Unit.UnitState;
import com.danwink.tacticshooter.gameobjects.Unit.UnitType;
import com.danwink.tacticshooter.network.Message;
import com.phyloa.dlib.util.DMath;

public class Fortifier extends ComputerPlayer 
{
	HashMap<Building, Integer> pointCount = new HashMap<Building, Integer>();
	
	int fortifierState = 0; //0 - Fortifying, 1 - Massing, 2 - Attacking
	
	Building closeb;
	Building target;
	
	int attackCountdown = 30;
	
	public void update( PathFinder finder ) 
	{
		if( fortifierState == 0 )
		{
			//Count units at points
			pointCount.clear();
			for( Building b : l.buildings )
			{
				if( b.bt == BuildingType.POINT && b.t != null && b.t.id == player.team.id )
				{
					pointCount.put( b, 0 );
					for( Unit u : units )
					{
						if( u.owner.id == player.id && u.stoppedAt == b )
						{
							Integer v = pointCount.get( u.stoppedAt );
							v++;
							pointCount.put( u.stoppedAt, v );
						}
					}
				}
			}
			
			//Find building with least units
			Building b = null;
			int unitsAtB = 100000;
			for( Entry<Building, Integer> e : pointCount.entrySet() )
			{
				if( e.getValue() < unitsAtB )
				{
					b = e.getKey();
					unitsAtB = e.getValue();
				}
			}
			
			if( b == null ) return;
			
			int unitCount = 0;
			
			for( Unit u : units )
			{
				if( u.owner.id == player.id )
				{
					unitCount++;
					if( u.state == UnitState.STOPPED && (u.stoppedAt == null || u.stoppedAt.bt != BuildingType.POINT) )
					{
						ArrayList<Integer> selected = new ArrayList<Integer>();
						selected.add( u.id );
						ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( b.x/Level.tileSize, b.y/Level.tileSize ), selected } ) );
					}
				}
			}
			
			if( unitCount >= 70 )
			{
				//Find enemy point with most units
				int maxUnits = 0;
				for( Building bu : l.buildings )
				{
					if( bu.t != null && bu.t.id != this.player.team.id )
					{
						int count = 0;
						for( Unit u : units )
						{
							if( u.stoppedAt != null && u.stoppedAt.id == bu.id )
							{
								count++;
							}
						}
						if( count > maxUnits )
						{
							maxUnits = count;
							target = bu;
						}
					}
				}
				
				//Find closest friendly building to target
				closeb = null;
				float closeDist = Float.MAX_VALUE;
				for( Building bu: l.buildings )
				{
					if( bu.t != null && bu.t.id == this.player.team.id && bu.hold == Building.HOLDMAX )
					{
						Path p = finder.findPath( null, l.getTileX( bu.x ), l.getTileY( bu.y ), l.getTileX( target.x ), l.getTileY( target.y ) );
						if( p == null ) continue;
						float d2 = p.getLength();
						if( d2 < closeDist )
						{
							closeDist = d2;
							closeb = b;
						}
					}
				}
				
				fortifierState = 1;
				attackCountdown = 30;
				ArrayList<Integer> selected = new ArrayList<Integer>();
				for( Unit u : units )
				{
					if( u.owner.id == player.id )
					{
						selected.add( u.id );
					}
				}
				ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( closeb.x/Level.tileSize, closeb.y/Level.tileSize ), selected } ) );
			}
		}
		else if( fortifierState == 1 )
		{	
			attackCountdown--;
			
			if( attackCountdown <= 0 )
			{
				attackCountdown = 30;
				fortifierState = 2;
				ArrayList<Integer> selected = new ArrayList<Integer>();
				
				for( Unit u : units )
				{
					if( u.owner.id == player.id )
					{
						selected.add( u.id );
					}
				}
				ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( target.x/Level.tileSize, target.y/Level.tileSize ), selected } ) );
			}
		}
		else if( fortifierState == 2 )
		{
			attackCountdown--;
			if( attackCountdown <= 0 ) fortifierState = 0;
		}
		
		if( player.money > 20 )
		{
			ci.sl.received( fc, new Message( MessageType.BUILDUNIT, UnitType.values()[DMath.randomi(0, UnitType.values().length)] ) );
		}
	}
}