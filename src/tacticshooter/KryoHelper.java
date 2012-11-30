package tacticshooter;

import java.util.ArrayList;

import javax.vecmath.Point2i;

import org.newdawn.slick.util.pathfinding.Path;

import com.esotericsoftware.kryo.Kryo;

public class KryoHelper 
{
	public static void register( Kryo k )
	{
		k.register( Message.class );
		k.register( Unit.class );
		k.register( Level.class );
		k.register( MessageType.class );
		k.register( int[].class );
		k.register( int[][].class );
		k.register( ArrayList.class );
		k.register( Point2i.class );
		k.register( Bullet.class );
	}
}
