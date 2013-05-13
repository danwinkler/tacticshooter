package tacticshooter;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point2i;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.opengl.shader.ShaderProgram;

import tacticshooter.Building.BuildingType;
import tacticshooter.Level.TileType;
import tacticshooter.Unit.UnitState;
import tacticshooter.Unit.UnitType;
import tacticshooter.scenegraph.Light;
import tacticshooter.scenegraph.Model;
import tacticshooter.scenegraph.Node;
import tacticshooter.scenegraph.World;

import com.danwink.tacticshooter.screens.MultiplayerGameScreen;
import com.phyloa.dlib.util.DMath;

public class GLLevelRenderer
{
	MultiplayerGameScreen mgs;
	
	public World world;
	
	Node floor;
	Node wall;
	
	Model simpleMan;
	Model house;
	Model flag;
	Model torus;
	Model mech;
	Model sniper;
	Model cube;
	Model saboteur;
	Model shotgun;
	Model healthBarModel;
	
	Light sun;
	
	HashMap<Integer, Node> units = new HashMap<Integer, Node>();
	HashMap<Integer, Node> buildings = new HashMap<Integer, Node>();
	
	Image man1Tex;
	
	Model[][] man1Models = new Model[4][4];
	
	public GLLevelRenderer( MultiplayerGameScreen mgs )
	{
		this.mgs = mgs;
		
		world = new World();
		
		mech = new Model( "data" + File.separator + "models" + File.separator + "voxelmech1.obj" );
		mech.rotateX( -DMath.PIF/2 );
		mech.rotateY( DMath.PIF/2 );
		
		house = new Model( "data" + File.separator + "models" + File.separator + "house1.obj" );
		house.rotateX( -DMath.PIF/2 );
		
		torus = new Model( "data" + File.separator + "models" + File.separator + "torus1.obj" );
		torus.rotateX( -DMath.PIF/2 );
		
		sniper = new Model( "data" + File.separator + "models" + File.separator + "voxelsniper1.obj" );
		sniper.rotateX( -DMath.PIF/2 );
		
		shotgun = new Model( "data" + File.separator + "models" + File.separator + "voxelshotgun.obj" );
		shotgun.rotateX( -DMath.PIF/2 );
		shotgun.rotateY( DMath.PIF/2 );
		
		saboteur = new Model( "data" + File.separator + "models" + File.separator + "voxelsaboteur.obj" );
		saboteur.rotateX( -DMath.PIF/2 );
		saboteur.rotateY( DMath.PIF/2 );
		
		cube = new Model( "data" + File.separator + "models" + File.separator + "cube.obj" );
		
		flag = new Model();
		flag.begin();
		GL11.glBegin( GL11.GL_QUADS );
		
		GL11.glNormal3f( 0, -1, 0 );
		
		GL11.glTexCoord2f( 0, 0 );
		GL11.glVertex3f( 0, 0, 0 );
	
		GL11.glTexCoord2f( 0, 1 );
		GL11.glVertex3f( 0, 0, -1 );
		
		GL11.glTexCoord2f( 1, 1 );
		GL11.glVertex3f( 1, 0, -1 );
		
		GL11.glTexCoord2f( 1, 0 );
		GL11.glVertex3f( 1, 0, 0 );
		
		GL11.glEnd();
		flag.end();
		
		healthBarModel = new Model();
		healthBarModel.begin();
		GL11.glBegin( GL11.GL_QUADS );
		
		GL11.glNormal3f( 0, 1, 0 );
		
		GL11.glTexCoord2f( 0, 0 );
		GL11.glVertex3f( -1, 0, -1 );
	
		GL11.glTexCoord2f( 0, 1 );
		GL11.glVertex3f( -1, 0, 1 );
		
		GL11.glTexCoord2f( 1, 1 );
		GL11.glVertex3f( 1, 0, 1 );
		
		GL11.glTexCoord2f( 1, 0 );
		GL11.glVertex3f( 1, 0, -1 );
		
		GL11.glEnd();
		healthBarModel.end();
		
		floor = new Node();
		floor.setModel( new Model() );
		world.add( floor );
		
		wall = new Node();
		wall.setModel( new Model() );
		world.add( wall );
		
		sun = new Light();
		sun.setPosition( 0, 1000, -200, false );
		world.add( sun );
		
		Light dimSum = new Light();
		dimSum.setPosition( 0, 1000, -200, false );
		dimSum.setAmbient( .2f, .2f, .2f, .2f );
		dimSum.setDiffuse( .2f, .2f, .2f, .2f );
		world.add( dimSum );
		
		world.setLightsEnabled( true );
		
		try
		{
			man1Tex = new Image( "img" + File.separator + "chicken.png" );
			
			float w4 = man1Tex.getWidth()/4;
			float h4 = man1Tex.getHeight()/4; 
			for( int x = 0; x < 4; x++ )
			{
				for( int y = 0; y < 4; y++ )
				{	
					Model m = new Model();
					m.begin();
					GL11.glBegin( GL11.GL_QUADS );
					GL11.glNormal3f( 0, 1, 0 );
					
					GL11.glTexCoord2f( x*.25f, y*.25f );
					GL11.glVertex3f( 0, 0, -1 );
					
					GL11.glTexCoord2f( x*.25f, (y+1)*.25f );
					GL11.glVertex3f( 0, 0, 0 );
					
					GL11.glTexCoord2f( (x+1)*.25f, (y+1)*.25f );
					GL11.glVertex3f( 1, 0, 0 );
					
					GL11.glTexCoord2f( (x+1)*.25f, y*.25f );
					GL11.glVertex3f( 1, 0, -1 );
					
					GL11.glEnd();
					m.end();
					man1Models[x][y] = m;
				}
			}
			
		} catch( SlickException e )
		{
			e.printStackTrace();
		}
	}
	
	public void render()
	{
		for( Unit u : mgs.cs.units )
		{
			Node n = units.get( u.id );
			n.mat.setIdentity();
			//n.rotateZ( u.heading );
			n.setPosition( u.x, u.y, n.getPosition().z );
			for( Node c : n.getChildren() )
			{
				if( c.getModel() == torus )
				{
					c.setVisible( u.selected );
				}
				else if( c.getModel() == healthBarModel )
				{
					c.mat.setIdentity();
					c.rotateZ( -u.heading + DMath.PIF );
					c.rotateX( DMath.PIF/4f );
					c.setScale( DMath.map( u.health, 0, u.type.health, 0, 12 ), .6f, 2 );
				}
			}	
		}

        GL11.glEnable( GL11.GL_BLEND );
        GL11.glEnable( GL11.GL_NORMALIZE );
		
		world.setCamera( mgs.cs.scrollx, mgs.cs.scrolly+mgs.zoom, -mgs.zoom*2 );
		world.setFocus( mgs.cs.scrollx, mgs.cs.scrolly-100, 0 );
		
		world.setUpCamera();
		
		//world.setShaderEnabled( true );
		world.render( mgs.gc.getWidth(), mgs.gc.getHeight() );
		//world.setShaderEnabled( false );
		
		mgs.ps.render( mgs );
		
		GL11.glDisable( GL11.GL_TEXTURE_2D );
		GL11.glColor3f( 0, 0, 0 );
		GL11.glLineWidth( 3 );
		GL11.glBegin( GL11.GL_LINES );
		for( int i = 0; i < mgs.cs.bullets.size(); i++ )
		{
			Bullet b = mgs.cs.bullets.get( i );
			GL11.glVertex3f( b.loc.x, b.loc.y, -Level.tileSize/2 );
			GL11.glVertex3f( b.loc.x+b.dir.x*.5f, b.loc.y+b.dir.y*.5f, -Level.tileSize/2 );
		}
		GL11.glEnd();
		
		for( Unit u : mgs.cs.units )
		{
			if( u.selected && u.state == UnitState.MOVING )
			{
				GL11.glColor3f( .5f, .5f, .8f );
				
				GL11.glBegin( GL11.GL_LINE_STRIP );
				for( int i = Math.max( u.onStep-2, 0 ); i < u.path.size(); i++ )
				{
					Point2i p1 = u.path.get( i );
					GL11.glVertex3f( (p1.x+.5f) * Level.tileSize, (p1.y+.5f) * Level.tileSize, -Level.tileSize/2 );
				}
				GL11.glEnd();
			}
		}
		GL11.glLineWidth( 1 );
	}
	
	public void setupMap()
	{
		Model fm = floor.getModel();
		Image bloodTexture = mgs.bloodTexture;
		floor.setTexture( bloodTexture.getTexture() );
		fm.begin();
		GL11.glBegin( GL11.GL_QUADS );
		GL11.glNormal3f( 0, 0, -1 );
		
		for( int y = 0; y < mgs.cs.l.height-1; y++ )
		{
			for( int x = 0; x < mgs.cs.l.width-1; x++ )
			{
				GL11.glTexCoord2f( bloodTexture.getTextureOffsetX() + bloodTexture.getTextureWidth()*((float)x/mgs.cs.l.width), 
						bloodTexture.getTextureOffsetY() + bloodTexture.getTextureHeight()*((float)y/mgs.cs.l.height) );
				GL11.glVertex3f( x*Level.tileSize, y*Level.tileSize, 0 );
				
				GL11.glTexCoord2f( bloodTexture.getTextureOffsetX() + bloodTexture.getTextureWidth()*((float)x/mgs.cs.l.width), 
						bloodTexture.getTextureOffsetY() + bloodTexture.getTextureHeight()*((float)(y+1)/mgs.cs.l.height) );
				GL11.glVertex3f( x*Level.tileSize, (y+1)*Level.tileSize, 0 );
				
				GL11.glTexCoord2f( bloodTexture.getTextureOffsetX() + bloodTexture.getTextureWidth()*((float)(x+1)/mgs.cs.l.width), 
						bloodTexture.getTextureOffsetY() + bloodTexture.getTextureHeight()*((float)(y+1)/mgs.cs.l.height) );
				GL11.glVertex3f( (x+1)*Level.tileSize, (y+1)*Level.tileSize, 0 );
				
				GL11.glTexCoord2f( bloodTexture.getTextureOffsetX() + bloodTexture.getTextureWidth()*((float)(x+1)/mgs.cs.l.width), 
						bloodTexture.getTextureOffsetY() + bloodTexture.getTextureHeight()*((float)y/mgs.cs.l.height) );
				GL11.glVertex3f( (x+1)*Level.tileSize, y*Level.tileSize, 0 );
			}
		}
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
					
					if( l.getTile( x, y-1 ) != TileType.WALL )
					{
						GL11.glNormal3f( 0, -1, 0 );
						
						GL11.glTexCoord2f( 0, 0 );
						GL11.glVertex3f( Level.tileSize*(x+1), Level.tileSize*y, -Level.tileSize );
						
						GL11.glTexCoord2f( 0, 1 );
						GL11.glVertex3f( Level.tileSize*(x+1), Level.tileSize*y, 0 );
						
						GL11.glTexCoord2f( 1, 1 );
						GL11.glVertex3f( Level.tileSize*(x+0), Level.tileSize*y, 0 );
						
						GL11.glTexCoord2f( 1, 0 );
						GL11.glVertex3f( Level.tileSize*(x+0), Level.tileSize*y, -Level.tileSize );
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
			if( b.bt == BuildingType.CENTER || b.bt == BuildingType.POINT )
			{
				bn = new Node();
				bn.setModel( house );
				bn.setPosition( b.x, b.y, 0 );
				bn.setScale( 20, 20, 20 );
				bn.setColor( new Color( 240, 230, 200 ) );
				world.add( bn );
				buildings.put( b.id, bn );
				
				Node fn = new Node();
				fn.setScale( .4f, .3f, 1 );
				fn.setModel( flag );
				bn.add( fn );
			}
		}
		if( b.bt == BuildingType.CENTER || b.bt == BuildingType.POINT )
		{
			Node fn = bn.getChildren().getFirst();
			fn.setScale( .4f, 1, .3f );
			Color c = b.t == null ? Color.white : b.t.getColor();
			fn.setColor( c );
			fn.setPosition( .33f, .37f, DMath.map( b.hold, 0, Building.HOLDMAX, -.3f, -1.1f ) );
		}
	}

	public void addUnit( Unit u )
	{
		Node unit = new Node();
		if( u.type == UnitType.HEAVY )
		{
			unit.setModel( mech );
			unit.setPosition( u.x, u.y, -8 );
			unit.setScale( 1.5f, 1.5f, 1.5f );
		}
		else if( u.type == UnitType.SNIPER )
		{
			unit.setModel( sniper );
			unit.setPosition( u.x, u.y, -6 );
			unit.setScale( 1.0f, 1.0f, 1.0f );
		}
		else if( u.type == UnitType.SHOTGUN )
		{
			unit.setModel( shotgun );
			unit.setPosition( u.x, u.y, -11 );
			unit.setScale( 1.0f, 1.0f, 1.0f );
		}
		else if( u.type == UnitType.SABOTEUR )
		{
			unit.setModel( saboteur );
			unit.setPosition( u.x, u.y, -8 );
			unit.setScale( 1.0f, 1.0f, 1.0f );
		}
		else
		{
			unit.setModel( man1Models[0][0] );
			unit.setPosition( u.x, u.y, 0 );
			unit.setScale( 10, 10, 10 );
			unit.setTexture( man1Tex.getTexture() );
			unit.setColor( Color.white );
		}
		//unit.setColor( u.owner.team.getColor() );
		units.put( u.id, unit );
		world.add( unit );
		
		Node t = new Node();
		t.setModel( torus );
		t.setVisible( false );
		t.setColor( new Color( 150, 150, 255 ) );
		t.setPosition( 0, 0, 0 );
		t.setScale( 6, 6, 6 );
		unit.add( t );
		
		Node health = new Node();
		health.setModel( healthBarModel );
		health.rotateX( -DMath.PIF/4f );
		health.setPosition( 0, 0, -12 );
		health.setColor( new Color( 150, 255, 150 ) );
		health.setScale( 12, .6f, 1 );
		unit.add( health );
	}
	
	public void removeUnit( Unit u )
	{
		units.remove( u.id ).markForDeletion();
	}
}
