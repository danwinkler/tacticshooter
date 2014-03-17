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

public class Aggressive extends ComputerPlayer 
{
	public void update( PathFinder finder ) 
	{
		for( Unit u : units )
		{
			if( u.owner.id == player.id && (u.state == UnitState.STOPPED || Math.random() < .1f) )
			{
				Building closeb = null;
				float closed2 = Float.MAX_VALUE;
				for( Building b : l.buildings )
				{
					boolean wantToTake = false;
					wantToTake = (b.t == null || b.t.id != player.team.id) && b.isCapturable( l, u, finder );
					
					if( wantToTake )
					{
						float dx = u.x-b.x;
						float dy = u.y-b.y;
						float d2 = dx*dx + dy*dy;
						if( d2 < closed2 )
						{
							closeb = b;
							closed2 = d2;
						}
					}
				}
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
