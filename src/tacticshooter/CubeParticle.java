package tacticshooter;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

import com.danwink.tacticshooter.screens.MultiplayerGameScreen;
import com.phyloa.dlib.particle.Particle;
import com.phyloa.dlib.util.DMath;

public class CubeParticle extends Particle<MultiplayerGameScreen>
{
	Vector3f angle;
	Vector3f dangle;
	public Color c = Color.red;
	
	public MultiplayerGameScreen mgs;
	
	public CubeParticle( float x, float y, float z, float dx, float dy, float dz )
	{
		super( x, y, z, dx, dy, dz, 10000 );
		angle = new Vector3f();
		dangle = new Vector3f( DMath.randomf( -50f, 50f ), DMath.randomf( -50f, 50f ), DMath.randomf( -50f, 50f ) );
	}
	
	public void update( float d )
	{
		super.update( d );
		angle.x += dangle.x * d;
		angle.y += dangle.y * d;
		angle.z += dangle.z * d;
		
		speed.z += .1f;
		
		if( pos.z > 0 && alive )
		{
			alive = false;
			mgs.drawBlood( pos.x, pos.y, c );
		}
	}
	
	public void render( MultiplayerGameScreen r )
	{
		GL11.glDisable( GL11.GL_TEXTURE_2D );
		if( !r.lr.world.isPlainRender() )
		{
			GL11.glColor3f( c.r, c.g, c.b );
		}
		GL11.glPushMatrix();
		GL11.glTranslatef( pos.x, pos.y, pos.z );
		GL11.glRotatef( angle.x, 1, 0, 0 );
		GL11.glRotatef( angle.y, 0, 1, 0 );
		GL11.glRotatef( angle.z, 0, 0, 1 );
		r.lr.cube.render();
		GL11.glPopMatrix();
	}
}
