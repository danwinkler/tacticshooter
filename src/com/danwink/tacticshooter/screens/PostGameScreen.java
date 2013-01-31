package com.danwink.tacticshooter.screens;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.GameStats;
import tacticshooter.GameStats.TeamStats;
import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;
import tacticshooter.StaticFiles;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;
import com.phyloa.dlib.util.DMath;

public class PostGameScreen extends DScreen<GameContainer, Graphics> implements DUIListener
{
	DUI dui;
	DButton okay;
	DButton rejoin;
	
	Slick2DRenderer r = new Slick2DRenderer();
	
	GameStats stats;
	
	public void onActivate( GameContainer e, DScreenHandler<GameContainer, Graphics> dsh )
	{
		dui = new DUI( new Slick2DEventMapper( e.getInput() ) );
		
		dui.add( new DText( "Post Game Stats", e.getWidth()/2 - 300, 30, true ) );
		
		int x = e.getWidth()/2-400;
		for( TeamStats ts : stats.teamStats )
		{
			dui.add( new DText( "Team: " + ts.t.id, x, 100 ) );
			dui.add( new DText( "Bullets Shot: " + ts.bulletsShot, x, 130 ) );
			dui.add( new DText( "Money Earned: " + ts.moneyEarned, x, 160 ) );
			dui.add( new DText( "Points Taken: " + ts.pointsTaken, x, 190 ) );
			dui.add( new DText( "Units Created: " + ts.unitsCreated, x, 220 ) );
			dui.add( new DText( "Units Lost: " + ts.unitsLost, x, 250 ) );
			x += 400;
		}
		
		okay = new DButton( "Okay", e.getWidth() / 2 - 200, e.getHeight() - 200, 200, 100 );
		rejoin = new DButton( "Rejoin", e.getWidth() / 2, e.getHeight() - 200, 200, 100 );
		
		StaticFiles.getMusic( "menu" ).loop();
		
		dui.add( okay );
		dui.add( rejoin );
		
		dui.add( new DText( "Points:", e.getWidth()/2 - 500, e.getHeight()/2 - 100 ) );
		dui.add( new DText( "Units:", e.getWidth()/2 - 500, e.getHeight()/2 + 100 ) );
		
		dui.addDUIListener( this );
		
		dui.setEnabled( true );
	}
	
	public void update( GameContainer gc, int delta )
	{
		dui.update();
	}

	public void render( GameContainer gc, Graphics g )
	{
		dui.render( r.renderTo( g ) );
		
		g.setLineWidth( 3 );
		
		g.pushTransform();
		g.translate( gc.getWidth()/2 - 400, gc.getHeight()/2 - 150 );
		g.setColor( Color.white );
		g.drawLine( 0, 0, 0, 100 );
		g.drawLine( 0, 100, 800, 100 );
		float yScale = 100.f / stats.totalPoints;
		for( TeamStats ts : stats.teamStats )
		{
			float xScale = 800.f / (ts.pointCount.size()-1);
			Color c = ts.t.getColor();
			g.setColor( new Color( c.r, c.g, c.b, 200 ) );
			for( int i = 0; i < ts.pointCount.size()-1; i++ )
			{
				g.drawLine( i*xScale, 100 - ts.pointCount.get( i ) * yScale, (i+1)*xScale, 100 - ts.pointCount.get( i+1 ) * yScale );
			}
		}
		g.popTransform();
		
		g.pushTransform();
		g.translate( gc.getWidth()/2 - 400, gc.getHeight()/2 + 50 );
		g.setColor( Color.white );
		g.drawLine( 0, 0, 0, 100 );
		g.drawLine( 0, 100, 800, 100 );
		float maxUnits = 0;
		for( TeamStats ts : stats.teamStats )
		{
			for( int i = 0; i < ts.unitCount.size(); i++ )
			{
				int c = ts.unitCount.get( i );
				if( c > maxUnits )
				{
					maxUnits = c;
				}
			}
		}
		yScale = 100.f / maxUnits;
		for( TeamStats ts : stats.teamStats )
		{
			float xScale = 800.f / (ts.unitCount.size()-1);
			Color c = ts.t.getColor();
			g.setColor( new Color( c.r, c.g, c.b, 200 ) );
			for( int i = 0; i < ts.unitCount.size()-1; i++ )
			{
				g.drawLine( i*xScale, 100 - ts.unitCount.get( i ) * yScale, (i+1)*xScale, 100 - ts.unitCount.get( i+1 ) * yScale );
			}
		}
		g.popTransform();
	}

	public void onExit()
	{
		dui.setEnabled( false );
		dui = null;
	}
	
	public void event( DUIEvent event )
	{
		DUIElement e = event.getElement();
		if( e instanceof DButton && event.getType() == DButton.MOUSE_UP )
		{
			if( e == okay )
			{
				dsh.activate( "home", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
			} else if( e == rejoin )
			{
				dsh.activate( "multiplayergame", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
			}
		}
	}

	@Override
	public void message( Object o )
	{
		stats = (GameStats)o;
	} 
}
