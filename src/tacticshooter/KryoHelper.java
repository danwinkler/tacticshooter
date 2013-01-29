package tacticshooter;

import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;

import tacticshooter.Unit.UnitType;

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
		k.register( Unit.UnitState.class );
		k.register( Object[].class );
		k.register( Team.class );
		k.register( Player.class );
		k.register( Building.class );
		k.register( Building.BuildingType.class );
		k.register( UnitType.class );
		k.register( Player[].class );
		k.register( Point2f.class );
		k.register( GameStats.class );
		k.register( GameStats.TeamStats.class );
		k.register( GameStats.TeamStats[].class );
		k.register( Level.TileType.class );
		k.register( Level.TileType[].class );
		k.register( Level.TileType[][].class );
		k.register( float[].class );
		k.register( float[][].class );
	}
}
