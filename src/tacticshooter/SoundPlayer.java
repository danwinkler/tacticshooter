package tacticshooter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import com.jogamp.openal.AL;
import com.jogamp.openal.ALC;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;

public class SoundPlayer
{
	static AL al;
	private static Vector loadedFiles = new Vector(); // Holds loaded file paths temporarily.

	private static Vector buffers = new Vector(); // Holds all loaded buffers.
	private static Vector sources = new Vector(); // Holds all validated sources.
	
	static ArrayList<Sound> sounds = new ArrayList<Sound>();
	
	static 
	{
		al = ALFactory.getAL();
		
		ALut.alutInit();
        al.alGetError();
        
	}
	
	static public void setPosition( float x, float y, float z )
	{
		al.alListener3f( AL.AL_POSITION, x, y, z );
	}
	
	static public int load( String filename )
	{
		try
		{
			sounds.add( new Sound( filename ) );
		} catch( IOException e )
		{
			e.printStackTrace();
		}
		return sounds.size() - 1;
	}
	
	static public void play( int sound )
	{
		sounds.get( sound ).play();
	}
	
	static public void play( int sound, float x, float y, float z )
	{
		sounds.get( sound ).play( x, y, z );
	}
	
	public static String getALCErrorString(int err) 
	{
	    switch(err) {
	        case ALC.ALC_NO_ERROR: return "ALC_NO_ERROR";
	        case ALC.ALC_INVALID_DEVICE: return "ALC_INVALID_DEVICE";
	        case ALC.ALC_INVALID_CONTEXT: return "ALC_INVALID_CONTEXT";
	        case ALC.ALC_INVALID_ENUM: return "ALC_INVALID_ENUM";
	        case ALC.ALC_INVALID_VALUE: return "ALC_INVALID_VALUE";
	        case ALC.ALC_OUT_OF_MEMORY: return "ALC_OUT_OF_MEMORY";
	        default: return null;
	    }
	}
	
	public static String getALErrorString(int err) 
	{
	    switch(err) {
	        case AL.AL_NO_ERROR: return "AL_NO_ERROR";
	        case AL.AL_INVALID_NAME: return "AL_INVALID_NAME";
	        case AL.AL_INVALID_ENUM: return "AL_INVALID_ENUM";
	        case AL.AL_INVALID_VALUE: return "AL_INVALID_VALUE";
	        case AL.AL_INVALID_OPERATION: return "AL_INVALID_OPERATION";
	        case AL.AL_OUT_OF_MEMORY: return "AL_OUT_OF_MEMORY";
	        default: return null;
	    }
	}
	
	public static int loadALBuffer(String path) throws IOException 
	{
	    // Variables to store data which defines the buffer.
	    int[] format = new int[1];
	    int[] size = new int[1];
	    ByteBuffer[] data = new ByteBuffer[1];
	    int[] freq = new int[1];
	    int[] loop = new int[1];

	    // Buffer id and error checking variable.
	    int[] buffer = new int[1];
	    int result;

	    // Generate a buffer. Check that it was created successfully.
	    al.alGenBuffers(1, buffer, 0);

	    if ((result = al.alGetError()) != AL.AL_NO_ERROR)
	        throw new IOException(getALErrorString(result));

	    // Read in the wav data from file. Check that it loaded correctly.

	    ALut.alutLoadWAVFile(path, format, data, size, freq, loop);

	    if ((result = al.alGetError()) != AL.AL_NO_ERROR)
	        throw new IOException(getALErrorString(result));

	    // Send the wav data into the buffer. Check that it was received properly.
	    al.alBufferData(buffer[0], format[0], data[0], size[0], freq[0]);

	    if ((result = al.alGetError()) != AL.AL_NO_ERROR)
	        throw new IOException(getALErrorString(result));

	    // Get rid of the temporary data.

	    //ALut.alutUnloadWAV(format[0], data[0], size[0], freq[0]);
	    
	    if ((result = al.alGetError()) != AL.AL_NO_ERROR)
	        throw new IOException(getALErrorString(result));

	    // Return the buffer id.
	    return buffer[0];
	}
	
	static int getLoadedALBuffer(String path) throws IOException 
	{
	    int count = 0; // 'count' will be an index to the buffer list.

	    int buffer; // Buffer id for the loaded buffer.


	    // Iterate through each file path in the list.
	    Iterator iter = loadedFiles.iterator();
	    int i = 0;
	    while( iter.hasNext() ) 
	    {
			String str = (String)iter.next();
	       if(str.equals(path)) 
	       {
	           return ((Integer)buffers.get(i)).intValue();
	       }
			i++;
		 }
	    // If we have made it this far then this file is new and we will create a buffer for it.
	    buffer = loadALBuffer( path );

	    // Add this new buffer to the list, and register that this file has been loaded already.
	    buffers.add( new Integer( buffer ) );

	    loadedFiles.add( path );

	    return buffer;
	}
	
	private static class Sound
	{
		int id;
		
		Sound( String filename ) throws IOException 
		{
			int[] source = new int[1];
		    int buffer;
		    int result;

		    // Get the files buffer id (load it if necessary).
		    buffer = getLoadedALBuffer( filename );

		    // Generate a source.
		    al.alGenSources(1, source, 0);

		    if ((result = al.alGetError()) != AL.AL_NO_ERROR)
		        throw new IOException(getALErrorString(result));

		    // Setup the source properties.

		    al.alSourcei (source[0], AL.AL_BUFFER,   buffer   );
		    al.alSourcef (source[0], AL.AL_PITCH,    1.0f      );
		    al.alSourcef (source[0], AL.AL_GAIN,     1.0f      );
		    al.alSource3f(source[0], AL.AL_POSITION, 0, 0, 0 );
		    al.alSource3f(source[0], AL.AL_VELOCITY, 0, 0, 0 );
		    al.alSourcei (source[0], AL.AL_LOOPING, AL.AL_FALSE );

		    // Save the source id.
		    sources.add(new Integer(source[0]));

		    // Return the source id.
		    id = source[0];
		}
		
		public void play()
		{
			al.alSourcePlay( id );
		}
		
		public void play( float x, float y, float z )
		{
			al.alSource3f( id, AL.AL_POSITION, x, y, z );
			al.alSourcePlay( id );
		}
	}
}
