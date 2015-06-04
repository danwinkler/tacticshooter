package com.danwink.tacticshooter.renderer;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.phyloa.dlib.util.DMath;

public class UnitBodyRenderer
{
	public Color playerColor = new Color( 128, 128, 255 );
	
	
	public void render( Graphics g, ClientState cs ) 
	{
		for( int i = 0; i < cs.units.size(); i++ )
		{
			Unit u = cs.units.get( i );
			drawUnit( g, u, cs );
		}
	}
	
	public void drawUnit( Graphics g, Unit u, ClientState cs )
	{
		g.pushTransform();
		g.translate( u.x, u.y );
		g.rotate( 0, 0, u.heading / DMath.PI2F * 360 );
		drawBody( g, u, u.owner.id == cs.player.id, cs );
		g.popTransform();
	}
		
	public void drawDeadUnit( Graphics g, Unit u, ClientState cs )
	{
		g.pushTransform();
		g.translate( u.x, u.y );
		g.rotate( 0, 0, u.heading / DMath.PI2F * 360 );
		drawBody( g, u, false, cs );
		g.popTransform();
	}
	
	public void drawBody( Graphics g, Unit u, boolean player, ClientState cs )
	{
		Color color = player ? playerColor : u.owner.team.getColor();
		
		switch( u.type )
		{
		case LIGHT:
			g.pushTransform();
			g.rotate( 0, 0, 90 );
			g.drawImage( cs.l.theme.light.getSprite( u.frame, 0 ), -8, -8 );
			g.drawImage( cs.l.theme.lightColor.getSprite( u.frame, 0 ), -8, -8, color );
			g.popTransform();
			break;
		case SCOUT:
		case SHOTGUN:
		case SABOTEUR:
			if( player )
			{
				g.setColor( Color.white );
				g.fillOval( -7, -7, 14, 14 );
				g.setColor( Color.black );
				g.drawOval( -7, -7, 14, 14 );
			}
			
			g.setColor( color );
			g.fillOval( -5, -5, 10, 10 );
			g.setColor( Color.black );
			g.drawOval( -5, -5, 10, 10 );
			g.drawLine( 0, 0, 5, 0 );
			
			break;
		case SNIPER:
			if( player )
			{
				g.setColor( Color.white );
				g.fillOval( -7, -6, 14, 12 );
				g.setColor( Color.black );
				g.drawOval( -7, -6, 14, 12 );
			}
			
			g.setColor( color );
			g.fillOval( -5, -4, 10, 8 );
			g.setColor( Color.black );
			g.drawOval( -5, -4, 10, 8 );
			g.drawLine( 0, 0, 5, 0 );
			break;
		case HEAVY:
			g.pushTransform();
			g.rotate( 0, 0, 90 );
			//g.scale( 1.0f, 1.0f );
			g.drawImage( cs.l.theme.heavy.getSprite( u.frame, 0 ), -16, -16 );
			g.drawImage( cs.l.theme.heavyColor.getSprite( u.frame, 0 ), -16, -16, color );
			g.popTransform();
			break;
			
		}
	}
}