package com.danwink.tacticshooter;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import com.phyloa.dlib.renderer.DScreenTransition;
import com.phyloa.dlib.util.DMath;

public class DScreenSlideTransition implements DScreenTransition<GameContainer, Graphics>
{
	float x, y;
	int xdir;
	int ydir;
	
	boolean slideIn;
	
	float startX;
	float startY;
	float goalX;
	float goalY;
	
	float time;
	float duration;
	
	public DScreenSlideTransition( int xdir, int ydir, float duration, boolean slideIn )
	{
		this.xdir = xdir;
		this.ydir = ydir;
		this.duration = duration;
		this.slideIn = slideIn;
	}
	
	public void init( GameContainer gc )
	{
		if( slideIn )
		{
			startX = gc.getWidth()*-xdir;
			startY = gc.getHeight()*-ydir;
		}
		else
		{
			goalX = gc.getWidth()*xdir;
			goalY = gc.getHeight()*ydir;
		}
		
		float v = slideIn ? expOut( time, 0, 1, duration ) : expIn( time, 0, 1, duration );
		
		x = DMath.lerp( v, startX, goalX );
		y = DMath.lerp( v, startY, goalY );
	}

	public void update( GameContainer gc, float d)
	{
		float v = slideIn ? expOut( time, 0, 1, duration ) : expIn( time, 0, 1, duration );
		
		x = DMath.lerp( v, startX, goalX );
		y = DMath.lerp( v, startY, goalY );
		
		time += d;
	}

	public void renderPre( GameContainer e, Graphics g )
	{
		g.pushTransform();
		g.translate( x, y );
	}
	
	public void renderPost( GameContainer e, Graphics g )
	{
		g.popTransform();
	}
	
	public boolean isFinished()
	{
		return time >= duration;
	}
	
	private static float expIn( float t, float b , float c, float d ) {
		return (t==0) ? b : c * (float)Math.pow(2, 10 * (t/d - 1)) + b;
	}

	private static float expOut( float t, float b , float c, float d ) {
		return (t==d) ? b+c : c * (-(float)Math.pow(2, -10 * t/d) + 1) + b;	
	}
}

