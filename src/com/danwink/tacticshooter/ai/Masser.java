package com.danwink.tacticshooter.ai;

import java.util.ArrayList;

import jp.objectclub.vecmath.Point2f;
import com.phyloa.dlib.math.Point2i;

import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.ComputerPlayer;
import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Unit.UnitState;
import com.danwink.tacticshooter.gameobjects.Unit.UnitType;
import com.danwink.tacticshooter.network.Message;
import com.phyloa.dlib.util.DMath;

public class Masser extends ComputerPlayer
{
	boolean attacking = false;
	public ArrayList<Unit> attackForce = new ArrayList<Unit>();
	float attackPropensity = DMath.randomf( 1.5f, 4 );
	public Building target;
	public Building closeb;
	
	{
		sleepDuration = 2000;
	}
	
	public void update( PathFinder finder )
	{		
		//Find enemy point with most units
		int maxUnits = 0;
		for( Building b : l.buildings )
		{
			if( b.t != null && b.t.id != this.player.team.id )
			{
				int count = 0;
				for( Unit u : units )
				{
					if( u.stoppedAt != null && u.stoppedAt.id == b.id )
					{
						count++;
					}
				}
				if( count > maxUnits )
				{
					maxUnits = count;
					target = b;
				}
			}
		}
		
		if( target == null ) return;
		
		//Find closest friendly building to target
		closeb = findBuildingShortestPath( new Point2f( target.x, target.y ), finder, new Filter<Building>() {
			public boolean valid( Building b ) {
				return b.t != null && b.t.id == Masser.this.player.team.id && b.hold == Building.HOLDMAX;
			}
		});
		
		if( !attacking )
		{
			//count own units, enemy units
			int ownUnits = 0;
			float enemyUnits = 0;
			for( Unit u : units )
			{
				if( u.owner.id == player.id && u.stoppedAt != null && closeb != null && u.stoppedAt.id == closeb.id )
				{
					ownUnits++;
				} 
				else if( u.stoppedAt != null && u.stoppedAt.id == target.id )
				{
					enemyUnits++;
				}
			}
			
			if( ownUnits >= (enemyUnits * attackPropensity) || Math.random() < .0025 )
			{
				attacking = true;
				
				attackForce.clear();
				
				ArrayList<Integer> selected = new ArrayList<Integer>();
				
				for( Unit u : units )
				{
					if( u.owner.id == player.id )
					{
						attackForce.add( u );
						selected.add( u.id );
					}
				}
				
				ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( target.x/Level.tileSize, target.y/Level.tileSize ), selected } ) );
			}
		}
		else
		{
			for( int i = 0; i < attackForce.size(); i++ )
			{
				Unit u = unitMap.get( attackForce.get( i ).id );
				if( u == null || !u.alive )
				{
					attackForce.remove( i );
					i--;
				}
			}
			if( attackForce.size() == 0 || (target.t != null && target.t.id == player.team.id) )
			{
				attacking = false;
				attackPropensity = DMath.randomf( 1.5f, 4f );
			}
		}
		
		for( Unit u : units )
		{
			if( u.owner.id == player.id && (u.state == UnitState.STOPPED || Math.random() < .1f) )
			{
				if( !attacking )
				{
					if( closeb == null ) break;
					if( u.stoppedAt != null && u.stoppedAt == closeb ) break;
					
					ArrayList<Integer> selected = new ArrayList<Integer>();
					selected.add( u.id );
					ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( closeb.x/Level.tileSize, closeb.y/Level.tileSize ), selected } ) );
				}	
			}
		}
		
		if( player.money > 20 )
		{
			ci.sl.received( fc, new Message( MessageType.BUILDUNIT, UnitType.values()[DMath.randomi(0, UnitType.values().length)] ) );
		}
	}
}
