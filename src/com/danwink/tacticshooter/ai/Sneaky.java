package com.danwink.tacticshooter.ai;

import java.util.ArrayList;

import javax.vecmath.Point2i;

import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.ComputerPlayer;
import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Unit.UnitState;
import com.danwink.tacticshooter.network.Message;
import com.phyloa.dlib.util.DMath;

public class Sneaky extends ComputerPlayer 
{
	public void update( PathFinder finder ) 
	{
		for( Unit u : units )
		{
			if( u.owner.id == player.id && (u.state == UnitState.STOPPED) )
			{
				Building closeb = null;
				for( Building b : l.buildings )
				{
					boolean wantToTake = b.isCapturable( l, u, finder ) && (b.t == null || b.t.id != player.team.id);
						
					if( wantToTake )
					{
						float dx = u.x-b.x;
						float dy = u.y-b.y;
						float d2 = dx*dx + dy*dy;
						if( d2 < 50 * 50 )
						{
							closeb = b;
							break;
						}
					}
				}
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
	}
}