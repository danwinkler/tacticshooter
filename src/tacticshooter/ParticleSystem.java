package tacticshooter;

import java.util.ArrayList;

import com.phyloa.dlib.renderer.Graphics2DRenderer;

@SuppressWarnings( "serial" )
public class ParticleSystem extends ArrayList<Particle>
{
	public ParticleSystem()
	{
		
	}
	
	public void update()
	{
		for( int i = 0; i < size(); i++ )
		{
			Particle p = get( i );
			get( i ).update();
			if( p.life <= 0 )
			{
				remove( i );
				i--;
			}
		}
	}
	
	public void render( Graphics2DRenderer g )
	{
		for( int i = 0; i < size(); i++ )
		{
			get( i ).render( g );
		}
	}
}
