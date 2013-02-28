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
		GL11.glEnable( GL11.GL_TEXTURE_2D );
		im.bind();
		GL11.glColor3f( c.r, c.g, c.b );
		GL11.glPushMatrix();
		GL11.glTranslatef( pos.x, pos.y, pos.z );
		billboard();
		GL11.glBegin( GL11.GL_QUADS );
		GL11.glNormal3f( 0, 0, -1 );
		
		GL11.glTexCoord2f( 0, 0 );
		GL11.glVertex3f( -size/2, -size/2, 0 );
		
		GL11.glTexCoord2f( 0, 1 );
		GL11.glVertex3f( size/2, -size/2, 0 );
		
		GL11.glTexCoord2f( 1, 1 );
		GL11.glVertex3f( size/2, size/2, 0 );
		
		GL11.glTexCoord2f( 1, 0 );
		GL11.glVertex3f( -size/2, size/2, 0 );
		
		GL11.glEnd();
		GL11.glPopMatrix();
	}
	
	void billboard()
	{
		FloatBuffer buf = BufferUtils.createFloatBuffer(16 * 4);
		// Get your current model view matrix from OpenGL. 
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf);
		buf.rewind();

		buf.put(0, 1.0f);
		buf.put(1, 0.0f);
		buf.put(2, 0.0f);

		buf.put(4, 0.0f);
		buf.put(5, 1.0f);
		buf.put(6, 0.0f);
		         
		buf.put(8, 0.0f);
		buf.put(9, 0.0f);
		buf.put(10, 1.0f);
		         
		GL11.glLoadMatrix(buf);
	}
}
