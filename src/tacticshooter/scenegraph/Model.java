package tacticshooter.scenegraph;

import org.lwjgl.opengl.GL11;

public class Model
{
	int callList;
	
	public Model()
	{
		callList = GL11.glGenLists( 1 );
	}
	
	public void begin()
	{
		GL11.glNewList( callList, GL11.GL_COMPILE );
	}
	
	public void end()
	{
		GL11.glEndList();
	}
	
	public void render()
	{
		GL11.glCallList( callList );
	}
}
