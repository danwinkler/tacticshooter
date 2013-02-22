package tacticshooter;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import com.danwink.tacticshooter.screens.MultiplayerGameScreen;
import com.phyloa.dlib.particle.Particle;
import com.phyloa.dlib.util.DMath;

public class WallParticle extends Particle<MultiplayerGameScreen>
{
	public static WallParticle makeParticle( float x, float y )
	{
		float angle = DMath.randomf( 0, DMath.PI2F );
		return new WallParticle( x, y, -Level.tileSize/2, DMath.cosf( angle ) * 5.f, DMath.sinf( angle ) * 5.f, DMath.randomf( -2, 2 ), DMath.randomf( .4f, 2f ) );
	}
	
	public WallParticle( float x, float y, float z, float dx, float dy, float dz, float duration )
	{
		super( x, y, z, dx, dy, dz, duration );
	}

	public void render( MultiplayerGameScreen r )
	{
		Graphics g = r.gc.getGraphics();
		g.setColor( Color.black );
		g.drawLine( pos.x, pos.y, pos.x+speed.x, pos.y+speed.y );
	}
}
