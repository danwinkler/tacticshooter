package com.danwink.tacticshooter.renderer;

import java.util.concurrent.ConcurrentLinkedDeque;

import jp.objectclub.vecmath.Point2f;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.gameobjects.Unit.UnitType;
import com.phyloa.dlib.util.DMath;

public class BloodExplosionRenderer
{
	private GameRenderer gameRenderer;

	BloodExplosionRenderer( GameRenderer gameRenderer )
	{
		this.gameRenderer = gameRenderer;
	}

	public Color bloodColor = new Color( 255, 0, 0 );
	
	public Image texture;
	Graphics tg;
	
	private ConcurrentLinkedDeque<Point2f> bloodToDraw = new ConcurrentLinkedDeque<>();
	
	public void render( Graphics g, ClientState cs, UnitBodyRenderer ubr ) 
	{
		if( texture == null )
		{
			if( cs.l != null )
			{
				generateTexture( cs );
			}
			else
			{
				return;
			}
		}
		
		while( !bloodToDraw.isEmpty() )
		{
			Point2f p = bloodToDraw.removeLast();
			internalDrawBlood( p.x, p.y, cs );
		}
		
		g.drawImage( texture, 0, 0 );
	}
	
	public void generateTexture( ClientState cs )
	{
		try
		{
			texture = new Image( cs.l.width * Level.tileSize, cs.l.height * Level.tileSize );
			tg = texture.getGraphics();
		}
		catch( SlickException e )
		{
			e.printStackTrace();
		}
	}
	
	public void killUnit( Unit u, ClientState cs, UnitBodyRenderer ubr )
	{
		ubr.drawDeadUnit( tg, u, cs );
		
		for( int j = 0; j < 10; j++ )
		{
			internalDrawBlood( u.x, u.y, cs );
		}
		
		if( u.type == UnitType.SABOTEUR )
		{
			tg.drawImage( cs.l.theme.crater, u.x - 16, u.y - 16, u.x + 16, u.y + 16, 0, 0, 32, 32 );
			tg.flush();
		}
	}
	
	public void drawBlood( float x, float y )
	{
		bloodToDraw.addFirst( new Point2f( x, y ) );
	}
	
	private void internalDrawBlood( float x, float y, ClientState cs )
	{
		x += DMath.randomf( -8, 8 );
		y += DMath.randomf( -8, 8 );
		
		tg.drawImage( cs.l.theme.smoke, x-4, y-4, x+4, y+4, 0, 0, 64, 64, bloodColor );
		tg.flush();
	}
}