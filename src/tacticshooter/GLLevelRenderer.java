package tacticshooter;

import java.io.File;
import java.io.FileNotFoundException;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Image;

import tacticshooter.scenegraph.Model;
import tacticshooter.scenegraph.Node;
import tacticshooter.scenegraph.World;

import com.danwink.tacticshooter.screens.MultiplayerGameScreen;
import com.phyloa.dlib.util.DMath;

public class GLLevelRenderer
{
	MultiplayerGameScreen mgs;
	
	World world;
	
	Node map;
	
	Model simpleMan;
	
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
		
		map = new Node();
		map.setModel( new Model() );
		
		world.add( map );
		
		setupMap();
	}
	
	public void render()
	{
		world.setCamera( mgs.cs.scrollx, mgs.cs.scrolly+mgs.zoom, mgs.zoom*2 );
		world.setFocus( mgs.cs.scrollx, mgs.cs.scrolly, 0 );
		world.render();
	}
	
	public void setupMap()
	{
		Model m = map.getModel();
		Image bloodTexture = mgs.bloodTexture;
		map.setTexture( bloodTexture.getTexture() );
		m.begin();
		GL11.glBegin( GL11.GL_QUADS );
		GL11.glNormal3f( 0, 0, 1 );
		GL11.glTexCoord2f( bloodTexture.getTextureOffsetX(), bloodTexture.getTextureOffsetY() );
		GL11.glVertex3f( 0, 0, 0 );

		GL11.glNormal3f( 0, 0, 1 );
		GL11.glTexCoord2f( bloodTexture.getTextureOffsetX(), bloodTexture.getTextureOffsetY() + bloodTexture.getTextureHeight() );
		GL11.glVertex3f( 0, bloodTexture.getHeight(), 0 );

		GL11.glNormal3f( 0, 0, 1 );
		GL11.glTexCoord2f( bloodTexture.getTextureOffsetX() + bloodTexture.getTextureWidth(), bloodTexture.getTextureOffsetY() + bloodTexture.getTextureHeight() );
		GL11.glVertex3f( bloodTexture.getWidth(), bloodTexture.getHeight(), 0 );

		GL11.glNormal3f( 0, 0, 1 );
		GL11.glTexCoord2f( bloodTexture.getTextureOffsetX() + bloodTexture.getTextureWidth(), bloodTexture.getTextureOffsetY() );
		GL11.glVertex3f( bloodTexture.getWidth(), 0, 0 );
		GL11.glEnd();
		m.end();
	}
}
