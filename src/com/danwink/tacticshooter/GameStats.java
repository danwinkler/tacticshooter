package com.danwink.tacticshooter;

import java.util.ArrayList;

import com.danwink.tacticshooter.gameobjects.Team;

public class GameStats
{
	public TeamStats[] teamStats;
	public int totalPoints;
	
	public static class TeamStats
	{	
		public ArrayList<Integer> pointCount = new ArrayList<Integer>();
		public ArrayList<Integer> unitCount = new ArrayList<Integer>();
		public int bulletsShot;
		public int unitsCreated;
		public int unitsLost;
		public int pointsTaken;
		public int moneyEarned;
		public Team t;
		
		public TeamStats( Team t )
		{
			this.t = t;
		}
		
		public TeamStats()
		{
			
		}
	}
	
	public TeamStats get( Team t )
	{
		return teamStats[t.id];
	}

	public void setup( Team... teams )
	{
		teamStats = new TeamStats[teams.length];
		for( int i = 0; i < teams.length; i++ )
		{
			Team t = teams[i];
			teamStats[t.id] = new TeamStats( t );
		}
	}
}
