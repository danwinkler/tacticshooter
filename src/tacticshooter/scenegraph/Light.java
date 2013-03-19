package tacticshooter.scenegraph;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Light
{
	FloatBuffer diffuse = BufferUtils.createFloatBuffer( 4 );
	FloatBuffer ambient = BufferUtils.createFloatBuffer( 4 );
	FloatBuffer specular = BufferUtils.createFloatBuffer( 4 );
	FloatBuffer position = BufferUtils.createFloatBuffer( 4 );
	
	float[] positionArr = { 0, 0, 0, 1 };
	
	public Light()
	{
		diffuse.put( new float[] { 1.0f, 1.0f, 1.0f, 1.0f } );
		ambient.put( new float[] { 0.1f, 0.1f, 0.1f, 1.0f } );
		specular.put( new float[] { 1.0f, 1.0f, 1.0f, 1.0f } );
		position.put( new float[] { 0, 0, 0, 1.0f } );
		
		diffuse.flip();
		ambient.flip();
		specular.flip();
		position.flip();
	}
	
	public void setPosition( float x, float y, float z, boolean directional )
	{
		positionArr[0] = x;
		positionArr[1] = y;
		positionArr[2] = z;
		positionArr[3] = directional ? 0 : 1;
		position.put( positionArr );
		position.flip();
	}
	
	public void setDiffuse( float x, float y, float z, float w )
	{
		diffuse.put( new float[] { x, y, z, w } );
		diffuse.flip();
	}
	
	public void enable( int index )
	{
		int light = GL11.GL_LIGHT0 + index;
		GL11.glEnable( light );
		
		GL11.glLight( light, GL11.GL_POSITION, position );
		GL11.glLight( light, GL11.GL_DIFFUSE, diffuse );
		GL11.glLight( light, GL11.GL_AMBIENT, ambient );
		GL11.glLight( light, GL11.GL_SPECULAR, specular );
	}
	
	public void disable( int index )
	{
		int light = GL11.GL_LIGHT0 + index;
		GL11.glDisable( light );
	}

	public void setAmbient( float x, float y, float z, float w )
	{
		ambient.put( new float[] { x, y, z, w } );
		ambient.flip();
	}
}
