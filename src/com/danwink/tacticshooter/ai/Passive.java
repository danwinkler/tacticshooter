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
import com.danwink.tacticshooter.network.Message;
import com.phyloa.dlib.util.DMath;

public class Passive extends ComputerPlayer 
{
	public void update( PathFinder finder ) 
	{
		
	}
}
