package com.danwink.tacticshooter.ai;

import java.util.ArrayList;

import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.ComputerPlayer;
import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Unit.UnitType;
import com.danwink.tacticshooter.network.Message;
import com.phyloa.dlib.math.Point2i;
import com.phyloa.dlib.util.DMath;

import jp.objectclub.vecmath.Point2f;

public class Good3 extends ComputerPlayer
{
	LevelAnalysis la;
	boolean battlePhase = false;
	
	public void update( PathFinder finder )
	{
		if( la == null )
		{
			la = new LevelAnalysis();
			la.build( l, finder );
		}
		
		if( !battlePhase ) expandPhase( finder );
		else battlePhase( finder );
		
	}
	
	//In this phase we try to expand and take every untaken point
	public void expandPhase( PathFinder finder )
	{
		//Build SCOUTS
		if( player.money >= UnitType.SCOUT.price )
		{
			ci.sl.received( fc, new Message( MessageType.BUILDUNIT, UnitType.SCOUT ) );
		}
		
		//If any unit is not moving, send it to the closest untaken point
		for( Unit u: units )
		{
			if( u.owner != player ) continue;
			
			if( u.state == Unit.UnitState.STOPPED ) 
			{
				Building b = findBuildingShortestPath( new Point2f( u.x, u.y ), finder, tb -> { return tb.t == null; });

				//If all points are taken, head to battlePhase
				if( b == null )
				{
					battlePhase = true;
					return;
				}
				
				moveUnit( u, new Point2i( b.x/Level.tileSize, b.y/Level.tileSize ) );
			}
		}
	}
	
	//This is the main game phase
	public void battlePhase( PathFinder finder )
	{
		//TODO: figure out army composition
		if( player.money >= UnitType.LIGHT.price )
		{
			ci.sl.received( fc, new Message( MessageType.BUILDUNIT, UnitType.LIGHT ) );
		}
		
		//If unit isn't a part of an army, either add it to a nearby army, or create a new one
		
		//TODO: figure out line of sight calculations (they are critical to gameplay)
		//For each army
		
			//If at home base
		
				//If more than n units (5?)
					//For each zone on border, give a score based on the relative strength vs the closest enemy zone
					//Send army to the weakest point
		
			//If not at home base
				//If close to another army
					//Merge armies
				//Look at closest enemy zone
					//Decide if you can attack, then attack if so
				
	}
	
	
	// Represents a group of units that should move together
	public class Army
	{
		ArrayList<Unit> units = new ArrayList<Unit>();
	}

}
