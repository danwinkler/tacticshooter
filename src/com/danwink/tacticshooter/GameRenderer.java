package com.danwink.tacticshooter;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.gameobjects.Level;

public class GameRenderer 
{
	/*
	 * Order of Rendering:
	 * 1. Outside of level texture (Shifted by viewport distance from nearest tile rounded down)
	 * 2. Floor Texture (Regenerated on first load, and when map changes)
	 * 3. Blood/Explosion Texture (Modified whenever a unit dies/blows up)
	 * 4. Wall Texture (Regenerated on first load, and when map changes)
	 * 5. Buildings
	 * 6. Unit bodies
	 * 7. Bullets
	 * 8. Particles
	 * 9. Unit Info (Paths if selected, name if mouse is nearby)
	 * 10. Fog of War (Generated each frame)
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
		bloodExplosion = new BloodExplosionRenderer();
		wall = new WallRenderer();
		building = new BuildingRenderer();
		unitBody = new UnitBodyRenderer();
		bullet = new BulletRenderer();
		particle = new ParticleSystemRenderer();
		unitInfo = new UnitInfoRenderer();
		fog = new FogRenderer();
	}
	
	public void render( Graphics g, ClientState cs, GameContainer gc )
	{
		outsideFloor.render( g, cs, gc );
		floor.render();
		bloodExplosion.render();
		wall.render();
		building.render();
		unitBody.render();
		bullet.render();
		particle.render();
		unitInfo.render();
		fog.render();
	}
	
	public class OutsideFloorRenderer
	{
		Image texture;
		
		public void render( Graphics g, ClientState cs, GameContainer gc )
		{
			if( texture == null ) 
			{
				if( cs.l != null ) 
				{
					generateTexture( cs, gc );
				}
				else
				{
					return;
				}
			}
			
			float x = -Level.tileSize-(cs.scrollx - ((int)(cs.scrollx/Level.tileSize))*Level.tileSize);
			float y = -Level.tileSize-(cs.scrolly - ((int)(cs.scrolly/Level.tileSize)*Level.tileSize));
			g.drawImage( texture, x, y );
		}
		
		private void generateTexture( ClientState cs, GameContainer gc )
		{
			try 
			{
				texture = new Image( gc.getWidth() + Level.tileSize*2, gc.getHeight() + Level.tileSize*2 );
				
				Graphics bgg = texture.getGraphics();
				for( int y = 0; y < gc.getHeight() + Level.tileSize*2; y += Level.tileSize )
				{
					for( int x = 0; x < gc.getWidth() + Level.tileSize*2; x += Level.tileSize )
					{	
						bgg.pushTransform();
						bgg.translate( x, y );
						AutoTileDrawer.draw( bgg, cs.l.theme.wall, Level.tileSize, 0, true, true, true, true, true, true, true, true );
						bgg.popTransform();
					}
				}
			} 
			catch( SlickException e )
			{
				e.printStackTrace();
			}
		}
	}
	
	public class FloorRenderer
	{
		public void render() {}
	}
	
	public class BloodExplosionRenderer
	{
		public void render() {}
	}
	
	public class WallRenderer
	{
		public void render() {}
	}
	
	public class BuildingRenderer
	{
		public void render() {}
	}
	
	public class UnitBodyRenderer
	{
		public void render() {}
	}
	
	public class BulletRenderer
	{
		public void render() {}
	}
	
	public class ParticleSystemRenderer
	{
		public void render() {}
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
