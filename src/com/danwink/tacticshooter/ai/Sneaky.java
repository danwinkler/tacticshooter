package com.danwink.tacticshooter.ai;

import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;

import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.ComputerPlayer;
import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.ComputerPlayer.Filter;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Unit.UnitState;
import com.danwink.tacticshooter.gameobjects.Unit.UnitType;
import com.danwink.tacticshooter.network.Message;
import com.phyloa.dlib.util.DMath;

/**
 * Tries to find a closeby enemy point. 
 * If one does not exist, choose a random enemy point and try to take it
 * @author Dan
 *
 */
public class Sneaky extends ComputerPlayer 
{
	public void update( PathFinder finder ) 
	{
		for( Unit u : units )
		{
			if( u.owner.id == player.id && (u.state == UnitState.STOPPED) )
			{
				//Find closest enemy building
				Building closeb = findBuildingClosest( new Point2f( u.x, u.y ), new Filter<Building>( u, finder ) {
					public boolean valid( Building b ) {
						return (b.t == null || b.t.id != player.team.id) && b.isCapturable( l, (Unit)o[0], (PathFinder)o[1] );
					}
				});
				
				//If its not close by
				if( closeb != null )
				{
					float dx = u.x-closeb.x;
					float dy = u.y-closeb.y;
					float d2 = dx*dx + dy*dy;
					if( d2 > 50 * 50 )
					{
						closeb = null;
						break;
					}
				}
				
				//Then go try to take a random enemy building
				if( closeb == null )
				{
					Building b = l.buildings.get( DMath.randomi( 0, DMath.randomi( 0, l.buildings.size() ) ) );
					if( b.isCapturable( l, u, finder ) && (b.t == null || b.t.id != player.team.id) )
					{
						ArrayList<Integer> selected = new ArrayList<Integer>();
						selected.add( u.id );
						ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( b.x/Level.tileSize, b.y/Level.tileSize ), selected } ) );
					}
				}
			}
		}
		
		if( player.money > 20 )
		{
			ci.sl.received( fc, new Message( MessageType.BUILDUNIT, UnitType.values()[DMath.randomi(0, UnitType.values().length)] ) );
		}
	}
}