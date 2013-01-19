package tacticshooter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class LevelFileHelper
{
	public static void saveLevel( String name, Level l ) throws IOException
	{
		Kryo kryo = new Kryo();
		KryoHelper.register( kryo );
		
		FileOutputStream fos = new FileOutputStream( "levels\\" + name );
		Output output = new Output( fos );
		kryo.writeObject( output, l );
		output.close();
		fos.close();
	}
	
	public static Level loadLevel( String name ) throws IOException
	{
		Kryo kryo = new Kryo();
		KryoHelper.register( kryo );
		
		FileInputStream fis = new FileInputStream( "levels\\" + name );
		
		Level l = kryo.readObject( new Input( fis ), Level.class );
		fis.close();
		return l;
	}
}
