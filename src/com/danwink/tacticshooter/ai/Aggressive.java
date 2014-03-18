package com.danwink.tacticshooter.ai;

import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;

import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.ComputerPlayer;
import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Unit.UnitState;
import com.danwink.tacticshooter.network.Message;

public class Aggressive extends ComputerPlayer 
{
	public void update( PathFinder finder ) 
	{
		for( Unit u : units )
		{
			if( u.owner.id == player.id && (u.state == UnitState.STOPPED || Math.random() < .1f) )
			{
				Building closeb = findBuildingClosest( new Point2f( u.x, u.y ), new Filter<Building>( u, finder ) {
					public boolean valid( Building b ) {
						return (b.t == null || b.t.id != player.team.id) && b.isCapturable( l, (Unit)o[0], (PathFinder)o[1] );
					}
				});
				if( closeb != null )
				{
					ArrayList<Integer> selected = new ArrayList<Integer>();
					selected.add( u.id );
					ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( closeb.x/Level.tileSize, closeb.y/Level.tileSize ), selected } ) );
				}
			}
		}
	}
}
