package com.danwink.tacticshooter.ai;

import java.util.ArrayList;
import java.util.HashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.ComputerPlayer;
import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.ai.LevelAnalysis.Neighbor;
import com.danwink.tacticshooter.ai.LevelAnalysis.Zone;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Unit.UnitState;
import com.danwink.tacticshooter.gameobjects.Unit.UnitType;
import com.danwink.tacticshooter.network.Message;
import com.phyloa.dlib.math.Point2i;

import jp.objectclub.vecmath.Point2f;

public class Good3 extends ComputerPlayer
{
	LevelAnalysis la;
	boolean battlePhase = false;
	ArrayList<Army> armies = new ArrayList<>();
	HashMap<Unit, Army> unitArmyMap = new HashMap<>();
	Building homeBase;
	
	public void update( PathFinder finder )
	{
		if( la == null )
		{
			la = new LevelAnalysis();
			la.build( l, finder );
			
			//Find Home Base
			for( Building b : l.buildings )
			{
				if( b.bt == BuildingType.CENTER && b.t.id == player.team.id )
				{
					homeBase = b;
					break;
				}
			}
		}
		assert( homeBase != null );
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
		
		//Update Armies
		for( int i = 0; i < armies.size(); i++ )
		{
			Army army = armies.get( i );
			army.update();
			if( army.units.size() == 0 )
			{
				armies.remove( i );
				i--;
			}
		}
		
		//If unit isn't a part of an army, either add it to a nearby army, or create a new one
		for( Unit u : units )
		{
			if( u.owner.id != player.id ) continue;
			
			if( !unitArmyMap.containsKey( u ) )
			{
				for( Army army : armies )
				{
					if( army.location.distance( new Point2f( u.x, u.y ) ) < Level.tileSize*5 )
					{
						army.units.add( u );
						unitArmyMap.put( u, army );
						break;
					}
				}
				//If we still didnt find an army to add to, start a new one
				if( !unitArmyMap.containsKey( u ) )
				{
					Army army = new Army();
					army.location = new Point2f( u.x, u.y );
					army.units.add( u );
					unitArmyMap.put( u, army );
					armies.add( army );
				}
			}
		}
		
		
		//TODO: figure out line of sight calculations (they are critical to gameplay)
		//For each army
		armyBreak:
		for( int i = 0; i < armies.size(); i++ )
		{
			Army army = armies.get( i );
			//TODO: possible add logic to change destination if things change along the way
			//Don't do army logic while moving
			if( army.moving ) continue;
			
			Zone bestBorderZone = findBestBorderZone();
			
			//If at home base and the home base is not the frontline
			if( army.location.distance( new Point2f( homeBase.x, homeBase.y ) ) < homeBase.radius*1.5f && bestBorderZone.b.id != homeBase.id )
			{
				//If more than n units (5?)
				if( army.units.size() > 5 )
				{
					//For each zone on border, give a score based on the relative strength vs the closest enemy zone
					
					//Send army to the weakest point
					if( bestBorderZone != null )
					{
						army.move( bestBorderZone.b.x, bestBorderZone.b.y );
						break armyBreak;
					}
				}
			}
			else //If not at home base
			{
				Zone currentZone = la.getZone( army.location );
				
				//If taking a point, never give up!
				if( currentZone.b.t.id != player.team.id )
				{
					continue;
				}
				
				//If close to another army Merge armies
				for( int j = 0; j < armies.size(); j++ )
				{
					Army bArmy = armies.get( j );
					if( bArmy == army || bArmy.moving ) continue;
					
					if( army.location.distance( bArmy.location ) < Level.tileSize * 3 )
					{
						//Add all units to this army
						for( Unit u : bArmy.units )
						{
							army.units.add( u );
							unitArmyMap.put( u, army );
						}
						
						//Remove army
						armies.remove( j );
						j--;
					}
				}
				
				//If not on a border zone
				//Move to a good border zone
				boolean borderZone = false;
				for( Neighbor n : currentZone.neighbors )
				{
					if( n.z.b.t.id != player.team.id )
					{
						borderZone = true;
						break;
					}
				}
				if( !borderZone )
				{
					Zone best = findBestBorderZone();
					//Send army to the weakest point
					if( best != null )
					{
						army.move( best.b.x, best.b.y );
						break armyBreak;
					}
				}
				
				//Look at closest enemy zone
					//Decide if you can attack, then attack if so
				
				for( Neighbor n : currentZone.neighbors )
				{
					if( n.z.b.t.id == player.team.id ) continue;
					
					if( !n.z.b.isCapturable( l ) ) continue;
					
					int nZoneStrength = numUnitsAtBuilding( n.z.b );
					
					if( nZoneStrength < army.units.size() * 2f )
					{
						army.move(  n.z.b.x, n.z.b.y );
						break armyBreak;
					}
				}
				
				//Finally, if there's nothing to do, check and see if we dwarf the enemy in size
				//If we do, choose an enemy point and attack it
				//This is because sometimes there are "natural" expansions behind the frontline, and the frontline 
				//is the enemy base, which we can't take. normally if this happens we are overpowering them anyway
				//so this check helps us not gridlock
				int ourSize = 0, theirSize = 0;
				for( Unit u: units )
				{
					if( u.owner.team.id == player.team.id )
					{
						ourSize++;
					}
					else
					{
						theirSize++;
					}
				}
				
				if( ourSize > theirSize * 2.5f )
				{
					for( Building b: l.buildings )
					{
						if( b.t.id != player.team.id && b.isCapturable( l ) )
						{
							army.move( b.x, b.y );
						}
					}
				}
			}
		}
				
	}
	
	public Zone findBestBorderZone()
	{
		//TODO: a zone that borders more of our own zones should get a higher score
		
		Zone best = null;
		int score = -1000;
		for( Zone z : la.zones )
		{
			if( z.b.t.id != player.team.id ) continue;
			
			int numUnits = 5; //Each point gets a base amount so even if its unoccupied its worth creating a front against
			boolean isBorder = false;
			for( Neighbor n : z.neighbors )
			{
				if( n.z.b.t.id != player.team.id )
				{
					numUnits += numUnitsAtBuilding( n.z.b );
					isBorder = true;
				}
			}
			
			int zScore = numUnits - numUnitsAtBuilding( z.b );
			
			if( !isBorder )
			{
				zScore -= 100;
			}
			
			if( zScore > score )
			{
				score = zScore;
				best = z;
			}
		}
		return best;
	}
	
	// Represents a group of units that should move together
	public class Army
	{
		Point2f location = new Point2f();
		ArrayList<Unit> units = new ArrayList<>();
		boolean moving = false;;
		
		public void update()
		{
			for( int i = 0; i < units.size(); i++ )
			{
				if( !units.get( i ).alive )
				{
					unitArmyMap.remove( units.remove( i ) );
					i--;
				}
			}
			
			if( moving )
			{
				boolean canStop = true;
				for( Unit u : units )
				{
					if( u.state == UnitState.MOVING )
					{
						canStop = false;
						break;
					}
				}
				if( canStop )
				{
					moving = false;
				}
			}
		}
		
		public void move( int x, int y )
		{
			location.x = x;
			location.y = y;
			ArrayList<Integer> unitIdsToMove = new ArrayList<Integer>();
			for( Unit u : units )
			{
				unitIdsToMove.add( u.id );
			}
			ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( x/Level.tileSize, y/Level.tileSize ), unitIdsToMove } ) );
			moving = true;
		}
	}
	
	
	//Debug render
	public void render( Graphics g )
	{
		la.render( g );
		g.setColor( Color.black );
		try
		{
			for( int i = 0; i < armies.size(); i++ )
			{
				Army a = armies.get( i );
				if( a.moving )
				{
					Point2f avgPoint = new Point2f();
					for( int j = 0; j < a.units.size(); j++ )
					{
						Unit u = a.units.get( i );
						avgPoint.x += u.x;
						avgPoint.y += u.y;
					}
					
					avgPoint.x /= a.units.size();
					avgPoint.y /= a.units.size();
					
					g.drawLine( avgPoint.x, avgPoint.y, a.location.x, a.location.y );
				}
			}
		} catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}
}
