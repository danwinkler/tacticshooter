package com.danwink.tacticshooter.screens;

import java.util.Map.Entry;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.GameStats;
import tacticshooter.GameStats.TeamStats;
import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class PostGameScreen implements DScreen<GameContainer, Graphics>, DUIListener
	{
		DScreenHandler<GameContainer, Graphics> dsh;
		
		DUI dui;
		DButton okay;
		
		Slick2DRenderer r = new Slick2DRenderer();
		
		GameContainer gc;
		
		GameStats stats;
		
		public void onActivate( GameContainer e, DScreenHandler<GameContainer, Graphics> dsh )
		{
			this.dsh = dsh;
			this.gc = e;
			dui = new DUI( new Slick2DEventMapper( e.getInput() ) );
			
			dui.add( new DText( "Post Game Stats", e.getWidth()/2 - 300, 30, true ) );
			
			int y = 100;
			for( TeamStats ts : stats.teamStats )
			{
				dui.add( new DText( "Team: " + ts.t.id, e.getWidth()/2 - 350, y ) );
				dui.add( new DText( "Bullets Shot: " + ts.bulletsShot, e.getWidth()/2 - 300, y+30 ) );
				dui.add( new DText( "Money Earned: " + ts.moneyEarned, e.getWidth()/2 - 300, y+60 ) );
				dui.add( new DText( "Points Taken: " + ts.pointsTaken, e.getWidth()/2 - 300, y+90 ) );
				dui.add( new DText( "Units Created: " + ts.unitsCreated, e.getWidth()/2 - 300, y+120 ) );
				dui.add( new DText( "Units Lost: " + ts.unitsLost, e.getWidth()/2 - 300, y+150 ) );
				y += 200;
			}
			
			okay = new DButton( "Okay", e.getWidth() / 2 - 100, e.getHeight() - 200, 200, 100 );
			
			dui.add( okay );
			
			dui.addDUIListener( this );
		}
		
		public void update( GameContainer gc, int delta )
		{
			dui.update();
		}

		public void render( GameContainer gc, Graphics g )
		{
			dui.render( r.renderTo( g ) );
		}

		public void onExit()
		{
			dui.setEnabled( false );
		}
		
		public void event( DUIEvent event )
		{
			DUIElement e = event.getElement();
			if( e instanceof DButton && event.getType() == DButton.MOUSE_UP )
			if( e == okay )
			{
				dsh.activate( "home", gc );
			} 	
		}

		@Override
		public void message( Object o )
		{
			stats = (GameStats)o;
		} 
}
