package com.danwink.tacticshooter.screens;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;


import com.danwink.tacticshooter.GameStats;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.GameStats.TeamStats;
import com.danwink.tacticshooter.slick.Slick2DEventMapper;
import com.danwink.tacticshooter.slick.Slick2DRenderer;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DLinePlot;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;
import com.phyloa.dlib.util.DMath;
import com.phyloa.dlib.util.DUtil;

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
		DLinePlot pointPlot = new DLinePlot( gc.getWidth()/2 - 400, gc.getHeight()/2 - 150, 800, 100 );
		for( TeamStats ts : stats.teamStats )
		{
			Color c = ts.t.getColor();
			pointPlot.addLine( DUtil.integerArrayListToIntArray( ts.pointCount ), new java.awt.Color( c.r, c.g, c.b, .8f ) );
		}
		dui.add( pointPlot );
		
		DLinePlot unitPlot = new DLinePlot( gc.getWidth()/2 - 400, gc.getHeight()/2 + 50, 800, 100 );
		for( TeamStats ts : stats.teamStats )
		{
			Color c = ts.t.getColor();
			unitPlot.addLine( DUtil.integerArrayListToIntArray( ts.unitCount ), new java.awt.Color( c.r, c.g, c.b, .8f ) );
		}
		dui.add( unitPlot );
		
		dui.addDUIListener( this );
		
		dui.setEnabled( true );
	}
	
	public void update( GameContainer gc, int delta )
	{
		dui.update();
	}

	public void render( GameContainer gc, Graphics g )
	{
		g.setColor( new Color( 0, 0, 0, 100 ) );
		g.fillRect( 0, 0, gc.getWidth(), gc.getHeight() );
		
		dui.render( r.renderTo( g ) );
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
				dsh.message( "connect", ((MultiplayerSetupScreen)dsh.get( "multiplayersetup" )).address.getText().trim() );
				dsh.activate( "connect", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
			}
		}
	}

	@Override
	public void message( Object o )
	{
		stats = (GameStats)o;
	} 
}
