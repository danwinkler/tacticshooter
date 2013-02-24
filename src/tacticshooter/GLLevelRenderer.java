package tacticshooter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.shader.ShaderProgram;

import tacticshooter.Level.TileType;
import tacticshooter.scenegraph.Light;
import tacticshooter.scenegraph.Model;
import tacticshooter.scenegraph.Node;
import tacticshooter.scenegraph.World;

import com.danwink.tacticshooter.screens.MultiplayerGameScreen;
import com.phyloa.dlib.util.DMath;

public class GLLevelRenderer
{
	MultiplayerGameScreen mgs;
	
	World world;
	
	Node floor;
	Node wall;
	
	Model simpleMan;
	Model house;
	Model flag;
	
	Light mouseLight;
	
	ShaderProgram shader;
	
	HashMap<Integer, Node> units = new HashMap<Integer, Node>();
	HashMap<Integer, Node> buildings = new HashMap<Integer, Node>();
	
	public GLLevelRenderer( MultiplayerGameScreen mgs )
	{
		this.mgs = mgs;
		
		world = new World();
		
		simpleMan = new Model();
		simpleMan.begin();
		try
		{
			ModelHelpers.loadModel( "data" + File.separator + "models" + File.separator + "simpleman1.obj" );
		} catch( FileNotFoundException e )
		{
			e.printStackTrace();
		}
		simpleMan.end();
		
		house = new Model();
		house.begin();
		try
		{
			ModelHelpers.loadModel( "data" + File.separator + "models" + File.separator + "house1.obj" );
		} catch( FileNotFoundException e )
		{
			e.printStackTrace();
		}
		house.end();
		
		flag = new Model();
		flag.begin();
		GL11.glBegin( GL11.GL_QUADS );
		
		GL11.glNormal3f( 0, 0, 1 );
		
		GL11.glTexCoord2f( 0, 0 );
		GL11.glVertex3f( 0, 0, 0 );
	
		GL11.glTexCoord2f( 0, 1 );
		GL11.glVertex3f( 0, 1, 0 );
		
		GL11.glTexCoord2f( 1, 1 );
		GL11.glVertex3f( 1, 1, 0 );
		
		GL11.glTexCoord2f( 1, 0 );
		GL11.glVertex3f( 1, 0, 0 );
		
		GL11.glEnd();
		flag.end();
		
		floor = new Node();
		floor.setModel( new Model() );
		world.add( floor );
		
		wall = new Node();
		wall.setModel( new Model() );
		world.add( wall );
		
		world.setLightsEnabled( true );
		
		
		mouseLight = new Light();
		world.add( mouseLight );
		
		try
		{
			shader = ShaderProgram.loadProgram( "data" + File.separator + "shaders" + File.separator + "toon.vert", "data" + File.separator + "shaders" + File.separator + "toon.frag" );
		} catch( SlickException e )
		{
			e.printStackTrace();
		}
	}
	
	public void render()
	{
		world.setCamera( mgs.cs.scrollx, mgs.cs.scrolly+mgs.zoom, -mgs.zoom*2 );
		world.setFocus( mgs.cs.scrollx, mgs.cs.scrolly, 0 );
		
		mouseLight.setPosition( mgs.mouseOnMap.x, mgs.mouseOnMap.y, -200 );
		mouseLight.setDiffuse( 1.0f, 1.0f, 1.0f, 1.0f );
		
		for( Unit u : mgs.cs.units )
		{
			Node n = units.get( u.id );
			n.mat.setIdentity();
			n.rotateX( -DMath.PIF/2 );
			n.setPosition( u.x, u.y, 0 );
		}
		
		GL11.glColor3f( 1, 1, 1 );
		
		shader.bind();
		world.render();
		shader.unbind();
	}
	
	public void setupMap()
	{
		Model fm = floor.getModel();
		Image bloodTexture = mgs.bloodTexture;
		floor.setTexture( bloodTexture.getTexture() );
		fm.begin();
		GL11.glBegin( GL11.GL_QUADS );
		GL11.glNormal3f( 0, 0, -1 );
		
		GL11.glTexCoord2f( bloodTexture.getTextureOffsetX(), bloodTexture.getTextureOffsetY() );
		GL11.glVertex3f( 0, 0, 0 );
		
		GL11.glTexCoord2f( bloodTexture.getTextureOffsetX(), bloodTexture.getTextureOffsetY() + bloodTexture.getTextureHeight() );
		GL11.glVertex3f( 0, bloodTexture.getHeight(), 0 );

		GL11.glTexCoord2f( bloodTexture.getTextureOffsetX() + bloodTexture.getTextureWidth(), bloodTexture.getTextureOffsetY() + bloodTexture.getTextureHeight() );
		GL11.glVertex3f( bloodTexture.getWidth(), bloodTexture.getHeight(), 0 );

		GL11.glTexCoord2f( bloodTexture.getTextureOffsetX() + bloodTexture.getTextureWidth(), bloodTexture.getTextureOffsetY() );
		GL11.glVertex3f( bloodTexture.getWidth(), 0, 0 );
		
		GL11.glEnd();
		fm.end();
		
		Model wm = wall.getModel();
		wm.begin();
		GL11.glBegin( GL11.GL_QUADS );
		Level l = mgs.cs.l;
		wall.setTexture( l.wall3d.getTexture() );
		for( int y = 0; y < l.height; y++ )
		{
			for( int x = 0; x < l.width; x++ )
			{
				if( l.getTile( x, y ) == TileType.WALL )
				{
					GL11.glNormal3f( 0, 0, -1 );
					GL11.glTexCoord2f( 0, 0 );
					GL11.glVertex3f( Level.tileSize*x, Level.tileSize*y, -Level.tileSize );
					
					GL11.glNormal3f( 0, 0, -1 );
					GL11.glTexCoord2f( 0, 1 );
					GL11.glVertex3f( Level.tileSize*x, Level.tileSize*(y+1), -Level.tileSize );
					
					GL11.glNormal3f( 0, 0, -1 );
					GL11.glTexCoord2f( 1, 1 );
					GL11.glVertex3f( Level.tileSize*(x+1), Level.tileSize*(y+1), -Level.tileSize );
					
					GL11.glNormal3f( 0, 0, -1 );
					GL11.glTexCoord2f( 1, 0 );
					GL11.glVertex3f( Level.tileSize*(x+1), Level.tileSize*y, -Level.tileSize );
				
					if( l.getTile( x, y+1 ) != TileType.WALL )
					{
						GL11.glNormal3f( 0, 1, 0 );
						
						GL11.glTexCoord2f( 0, 0 );
						GL11.glVertex3f( Level.tileSize*x, Level.tileSize*(y+1), -Level.tileSize );
						
						GL11.glTexCoord2f( 0, 1 );
						GL11.glVertex3f( Level.tileSize*x, Level.tileSize*(y+1), 0 );
						
						GL11.glTexCoord2f( 1, 1 );
						GL11.glVertex3f( Level.tileSize*(x+1), Level.tileSize*(y+1), 0 );
						
						GL11.glTexCoord2f( 1, 0 );
						GL11.glVertex3f( Level.tileSize*(x+1), Level.tileSize*(y+1), -Level.tileSize );
					}
					
					if( l.getTile( x-1, y ) != TileType.WALL )
					{
						GL11.glNormal3f( -1, 0, 0 );
						
						GL11.glTexCoord2f( 0, 0 );
						GL11.glVertex3f( Level.tileSize*x, Level.tileSize*y, -Level.tileSize );
						
						GL11.glTexCoord2f( 0, 1 );
						GL11.glVertex3f( Level.tileSize*x, Level.tileSize*y, 0 );
						
						GL11.glTexCoord2f( 1, 1 );
						GL11.glVertex3f( Level.tileSize*x, Level.tileSize*(y+1), 0 );
						
						GL11.glTexCoord2f( 1, 0 );
						GL11.glVertex3f( Level.tileSize*x, Level.tileSize*(y+1), -Level.tileSize );
					}
					
					if( l.getTile( x+1, y ) != TileType.WALL )
					{
						GL11.glNormal3f( 1, 0, 0 );
						
						GL11.glTexCoord2f( 0, 0 );
						GL11.glVertex3f( Level.tileSize*(x+1), Level.tileSize*(y+1), -Level.tileSize );
						
						GL11.glTexCoord2f( 0, 1 );
						GL11.glVertex3f( Level.tileSize*(x+1), Level.tileSize*(y+1), 0 );
						
						GL11.glTexCoord2f( 1, 1 );
						GL11.glVertex3f( Level.tileSize*(x+1), Level.tileSize*(y+0), 0 );
						
						GL11.glTexCoord2f( 1, 0 );
						GL11.glVertex3f( Level.tileSize*(x+1), Level.tileSize*(y+0), -Level.tileSize );
					}
				}
			}
		}
		GL11.glEnd();
		wm.end();
	}
	
	public void updateBuilding( Building b )
	{
		Node bn = buildings.get( b.id );
		if( bn == null )
		{
			bn = new Node();
			bn.setModel( house );
			bn.rotateX( -DMath.PIF/2 );
			bn.setPosition( b.x, b.y, 0 );
			bn.setScale( 20, 20, 20 );
			world.add( bn );
			buildings.put( b.id, bn );
			
			Node fn = new Node();
			fn.setScale( .4f, .3f, 0 );
			fn.setModel( flag );
			bn.add( fn );
		}
		
		Node fn = bn.getChildren().getFirst();
		fn.setScale( .4f, .3f, 0 );
		Color c = b.t == null ? Color.white : b.t.getColor();
		fn.setColor( c );
		fn.setPosition( .33f, DMath.map( b.hold, 0, Building.HOLDMAX, .3f, 1.1f ), .37f );
	}

	public void addUnit( Unit u )
	{
		Node unit = new Node();
		unit.setModel( simpleMan );
		unit.rotateY( -DMath.PIF/2 );
		unit.setPosition( u.x, u.y, 0 );
		unit.setScale( 4, 4, 4 );
		unit.setColor( u.owner.team.getColor() );
		units.put( u.id, unit );
		world.add( unit );
	}
	
	public void removeUnit( Unit u )
	{
		units.remove( u.id ).markForDeletion();
	}
}
