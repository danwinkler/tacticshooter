package tacticshooter;

import java.nio.FloatBuffer;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import com.danwink.tacticshooter.screens.MultiplayerGameScreen;
import com.phyloa.dlib.particle.Particle;
import com.phyloa.dlib.util.DMath;

public class ExplodeParticle extends Particle<MultiplayerGameScreen>
{
	public Color c;
	float maxdur;
	public float size;
	public Image im;
	
	public ExplodeParticle( float x, float y, float z, float dx, float dy, float dz, float duration )
	{
		super( x, y, z, dx, dy, dz, duration );
		maxdur = duration;
	}

	public void update( float d )
	{
		super.update( d );
		c.a = .5f * DMath.minf( ((timeleft*2)/maxdur), 1 );
	}
	
	public void render( MultiplayerGameScreen g )
	{
		im.bind();
		Graphics r = g.gc.getGraphics();
		r.setColor( c );
		GL11.glPushMatrix();
		GL11.glTranslatef( pos.x, pos.y, pos.z );
		rotateToFace( g.eye );
		GL11.glBegin( GL11.GL_QUADS );
		r.drawImage( im, -size/2, -size/2, size/2, size/2, 0, 0, 64, 64, c );
		GL11.glPopMatrix();
	}
	
	static Vector3f up = new Vector3f( 0, -2, -1 );
	void rotateToFace( Point3f point )
	{
	  Vector3f d = new Vector3f();
	  d.set( pos );
	  d.sub( point );
	  Vector3f right = new Vector3f();
	  right.cross( up, d );
	  right.normalize();
	  Vector3f backwards = new Vector3f();
	  backwards.cross( right, up );
	  backwards.normalize();
	  Vector3f up2 = new Vector3f();
	  up2.cross( backwards, right );
	  FloatBuffer fb = BufferUtils.createFloatBuffer( 16 );
	  fb.put( new float[] { 
			  right.x, right.y, right.z, 0, 
			  up.x, up.y, up.z, 0, 
			  backwards.x, backwards.y, backwards.z, 0, 
			  0, 0, 0, 1 } );
	  fb.flip();
	  GL11.glMultMatrix( fb );
	}
}
