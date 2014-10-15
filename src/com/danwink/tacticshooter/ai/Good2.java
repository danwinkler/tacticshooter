package com.danwink.tacticshooter.ai;

import java.util.ArrayList;

import org.newdawn.slick.util.pathfinding.Path;
import org.newdawn.slick.util.pathfinding.PathFinder;

import com.danwink.tacticshooter.ComputerPlayer;
import com.danwink.tacticshooter.MessageType;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Unit.UnitState;
import com.danwink.tacticshooter.gameobjects.Unit.UnitType;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.network.Message;
import com.phyloa.dlib.math.Point2i;
import com.phyloa.dlib.util.DMath;

public class Good2 extends ComputerPlayer 
{
	public static float weightBuildingOwnerNotTaken;
	public static float weightBuildingOwnerEnemy;
	public static float weightBuildingOwnerTeam;
	public static float weightBuildingEachEnemy;
	public static float weightBuildingEachFriend;
	public static float weightBuildingEachEnemyOnOwnedPoint;
	public static float weightBuildingDistanceFromTeamPoint;
	public static float weightBuildingInversePointOwnershipAmount;
	
	public static float weightUnitAtBase;
	public static float weightUnitDistanceFromEnemyPoint;
	public static float weightUnitIsNotStopped;
	public static float weightUnitIsStoppedOnEnemy;
	public static float weightUnitDistanceFromTarget;
	public static float weightUnitTargetMultiplier;
	
	public static float unitScoreThreshold;
	
	public Good2()
	{
		setValues();
	}
	
	
	public void setValues()
	{
		//POINT SEARCH WEIGHTS
		
		//Point ownership
		//The idea is that open points should be the priority, and then try to take enemy points.
		//Placing units on owned points doesn't really make a big difference
		weightBuildingOwnerNotTaken = 50;
		weightBuildingOwnerEnemy = 20;
		weightBuildingOwnerTeam = 1;
		
		//Units on point
		//Fewer units is better, enemies are worse than friends.
		weightBuildingEachEnemy = -.5f;
		weightBuildingEachFriend = -.25f;
		//Enemies on own point means you want to attack
		weightBuildingEachEnemyOnOwnedPoint = 4f;
		
		//Distance from point owned by team
		weightBuildingDistanceFromTeamPoint = -.2f;
		
		//Point owned by team but not fully taken
		weightBuildingInversePointOwnershipAmount = .02f;
		
		//UNIT SELECTION WEIGHTS
		weightUnitAtBase = 10;
		
		//Distance from closest enemy point
		//Further away you are, more likely to be chosen
		weightUnitDistanceFromEnemyPoint = 10f;
		
		//If unit is not stopped, much less likely to be chosen
		weightUnitIsNotStopped = -1000;
		
		//If unit is stopped on enemy building, much less likely to be chosen
		weightUnitIsStoppedOnEnemy = -15;
		
		//If unit is close to target, more likely to be chosen
		weightUnitDistanceFromTarget = -.1f;
		
		//If target got a high score, translate that to more units being chosen
		weightUnitTargetMultiplier = .1f;
		
		unitScoreThreshold = 5;
	}
	
	public void update( PathFinder finder ) 
	{
		setValues();
		//Stages of AI
		// 1. POINT SEARCH - find the best building for the next action
		// 2. UNIT SELECTION - Choose Units to carry out action
		// 3. SEND TO SERVER - Send command(s) to server
		
		// POINT SEARCH
		float targetBuildingScore = Float.NEGATIVE_INFINITY;
		Building targetBuilding = null;
		for( Building b : l.buildings )
		{
			float score = 0;
			
			//Skip if the building isn't capturable
			if( !b.isCapturable( l ) ) { continue; }
			
			//If is capturable, and is the final point, all in
			if( b.bt == BuildingType.CENTER )
			{
				score += 1000000;
			}
			
			//Add weights based on point ownership
			if( b.t == null ) { score += weightBuildingOwnerNotTaken; }
			else if( b.t.id == player.team.id ) 
			{ 
				score += weightBuildingOwnerTeam;
				score += (b.HOLDMAX - b.hold) * weightBuildingInversePointOwnershipAmount;
			}
			else { score += weightBuildingOwnerEnemy; }
			
			//Add weights based on units on the point
			for( Unit u : units )
			{
				if( u.stoppedAt != null && u.stoppedAt.id == b.id )
				{
					if( u.owner.team.id == player.team.id )
					{
						score += weightBuildingEachFriend;
					}
					else
					{
						score += weightBuildingEachEnemy;
						if( b.t != null && b.t.id == player.team.id ) 
						{
							score += weightBuildingEachEnemyOnOwnedPoint;
						}
					}
				}
			}
			
			//Add weights based on shortest distance to point owned by team (shorter is better)
			Building closestTeamBuilding = null;
			float closestTeamBuildingDistance = Float.POSITIVE_INFINITY;
			for( Building bu : l.buildings )
			{
				if( bu.t != null && bu.t.id == this.player.team.id && bu.hold == Building.HOLDMAX )
				{
					Path p = finder.findPath( null, l.getTileX( bu.x ), l.getTileY( bu.y ), l.getTileX( b.x ), l.getTileY( b.y ) );
					if( p == null ) continue;
					float d2 = p.getLength();
					if( d2 < closestTeamBuildingDistance )
					{
						closestTeamBuildingDistance = d2;
						closestTeamBuilding = bu;
					}
				}
			}
			score += closestTeamBuildingDistance * weightBuildingDistanceFromTeamPoint;
		
			if( score > targetBuildingScore )
			{
				targetBuilding = b;
				targetBuildingScore = score;
			}
		}
		
		// UNIT SELECTION
		ArrayList<Unit> selectedUnits = new ArrayList<Unit>();
		
		for( Unit u : units )
		{
			float score = 0;
			
			//If unit is at base
			if( u.stoppedAt != null && u.stoppedAt.bt == BuildingType.CENTER ) 
			{
				score += weightUnitAtBase;
			}
			
			//Distance from nearest enemy point
			Building closestBuildingToUnit = null;
			float closestBuildingToUnitDistance = Float.POSITIVE_INFINITY;
			for( Building bu : l.buildings )
			{
				if( bu.t != null && bu.t.id == this.player.team.id && bu.hold == Building.HOLDMAX )
				{
					Path p = finder.findPath( null, l.getTileX( bu.x ), l.getTileY( bu.y ), l.getTileX( u.x ), l.getTileY( u.y ) );
					if( p == null ) continue;
					float d2 = p.getLength();
					if( d2 < closestBuildingToUnitDistance )
					{
						closestBuildingToUnitDistance = d2;
						closestBuildingToUnit = bu;
					}
				}
			}
			score += closestBuildingToUnitDistance * weightUnitDistanceFromEnemyPoint;
			
			//Unit is moving
			if( u.state != UnitState.STOPPED )
			{
				score += weightUnitIsNotStopped;
			}
			
			//Unit on enemy point
			if( u.stoppedAt != null && u.stoppedAt.t.id != player.team.id )
			{
				score += weightUnitIsStoppedOnEnemy;
			}
			
			//Unit distance from target
			Path p = finder.findPath( null, l.getTileX( targetBuilding.x ), l.getTileY( targetBuilding.y ), l.getTileX( u.x ), l.getTileY( u.y ) );
			if( p != null ) 
			{
				score += p.getLength() * weightUnitDistanceFromTarget;
			}
			
			//Add in target score
			score += targetBuildingScore * weightUnitTargetMultiplier;
			
			//If target is CENTER then ALL IN attack
			if( targetBuilding.bt == BuildingType.CENTER )
			{
				score += 100000;
			}
			
			if( score > unitScoreThreshold ) 
			{
				selectedUnits.add( u );
			}
		}
		
		ArrayList<Integer> unitIdsToMove = new ArrayList<Integer>();
		for( Unit u : selectedUnits )
		{
			unitIdsToMove.add( u.id );
		}
		ci.sl.received( fc, new Message( MessageType.SETATTACKPOINT, new Object[]{ new Point2i( targetBuilding.x/Level.tileSize, targetBuilding.y/Level.tileSize ), unitIdsToMove } ) );
		
		//TODO: figure out better way to choose what kind of unit to buy, and when to buy it
		if( player.money > 20 )
		{
			ci.sl.received( fc, new Message( MessageType.BUILDUNIT, UnitType.values()[DMath.randomi(0, UnitType.values().length)] ) );
		}
	}
}

