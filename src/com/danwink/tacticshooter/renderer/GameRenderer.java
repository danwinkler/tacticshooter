package com.danwink.tacticshooter.renderer;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.ExplodeParticle;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.danwink.tacticshooter.renderer.OutsideFloorRenderer;
import com.phyloa.dlib.util.DMath;

public class GameRenderer 
{
	/*
	 * Order of Rendering:
	 * 1. Outside of level texture (Shifted by viewport distance from nearest tile rounded down)
	 * 
	 * 2. Translate to viewport 
	 * 
	 * 3. Floor Texture (Regenerated on first load, and when map changes)
	 * 4. Blood/Explosion Texture (Modified whenever a unit dies/blows up)
	 * 5. Buildings
	 * 6. Unit bodies
	 * 7. Wall Texture (Regenerated on first load, and when map changes)
	 * 8. Bullets
	 * 9. Particles
	 * 10. Unit Info (Paths if selected, name if mouse is nearby)
	 * 11. Fog of War (Generated each frame)
	 */
	
	OutsideFloorRenderer outsideFloor;
	FloorRenderer floor;
	BloodExplosionRenderer bloodExplosion;
	WallRenderer wall;
	BuildingRenderer building;
	UnitBodyRenderer unitBody;
	BulletRenderer bullet;
	ParticleSystemRenderer particle;
	UnitInfoRenderer unitInfo;
	FogRenderer fog;
	
	public GameRenderer()
	{
		outsideFloor = new OutsideFloorRenderer();
		floor = new FloorRenderer();
		bloodExplosion = new BloodExplosionRenderer(this);
		building = new BuildingRenderer();
		unitBody = new UnitBodyRenderer();
		wall = new WallRenderer();
		bullet = new BulletRenderer();
		particle = new ParticleSystemRenderer();
		unitInfo = new UnitInfoRenderer();
		fog = new FogRenderer();
	}
	
	public void render( Graphics g, ClientState cs, GameContainer gc )
	{
		outsideFloor.render( g, cs, gc );
		
		g.pushTransform();
		g.translate( -cs.scrollx, -cs.scrolly );
		
		floor.render( g, cs );
		bloodExplosion.render( g, cs, unitBody );
		building.render();
		unitBody.render( g, cs );
		wall.render( g, cs );
		bullet.render();
		particle.render();
		unitInfo.render();
		fog.render();
			
		//TODO: uncomment when all renderers are completed
		//g.popTransform();
	}
	
	public void killUnit( Unit u )
	{
		bloodExplosion.killUnit( u );
	}
	
	public void drawBlood( float x, float y )
	{
		bloodExplosion.drawBlood( x, y );
	}
	
	public void createExplosion( float x, float y )
	{
		particle.createExplosion( x, y );
	}
	
	public class WallRenderer
	{
		Image texture;
		
		public void render( Graphics g, ClientState cs ) 
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
			
			g.drawImage( texture, 0, 0 );
		}
		
		private void generateTexture( ClientState cs )
		{
			try
			{
				texture = new Image( cs.l.width * Level.tileSize, cs.l.height * Level.tileSize );
				cs.l.render( texture.getGraphics() );
			}
			catch( SlickException e )
			{
				e.printStackTrace();
			}
		}
	}
	
	public class BuildingRenderer
	{
		public void render() {}
	}
	
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
	
	public class BulletRenderer
	{
		public void render() {}
	}
	
	public class ParticleSystemRenderer
	{
		public void render() 
		{
			
		}
		
		public void createExplosion( float x, float y )
		{
			/*
			for( int j = 0; j < 7; j++ )
			{
				float magmax = DMath.randomf( .4f, 1 );
				float heading = DMath.randomf( 0, DMath.PI2F );
				for( float mag = .1f; mag < 1; mag += .1f )
				{
					float r = DMath.lerp( mag, .5f, 1f );
					float g = DMath.lerp( mag, .5f, .25f );
					float b = DMath.lerp( mag, .5f, 0f );
					ExplodeParticle p = new ExplodeParticle( u.x, u.y, DMath.cosf( heading ) * 25 * mag * magmax, DMath.sinf( heading ) * 25 * mag * magmax, 30 );
					p.c = new Color( r, g, b );
					p.friction = .075f;
					p.im = cs.l.theme.smoke;
					p.size = (1.f-mag) * magmax * 20;
					ps.add( p );
				}
			}
			*/
		}
	}
	
	public class UnitInfoRenderer
	{
		public void render() {}
	}
	
	public class FogRenderer
	{
		public void render() {}
	}
}
