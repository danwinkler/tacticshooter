package tacticshooter.scenegraph;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class World
{
	Node root = new Node();
	
	Point3f camera = new Point3f();
	Point3f focus = new Point3f();
	Vector3f up = new Vector3f( 0, 0, 1 );
	
	public void render()
	{
		GLU.gluLookAt( -camera.x, camera.y, camera.z, -focus.x, focus.y, focus.z, up.x, up.y, up.z );
		GL11.glPushMatrix();
		GL11.glScalef( -1, 1, 1 );
		root.render();
		GL11.glPopMatrix();
	}
	
	public void setCamera( float x, float y, float z )
	{
		camera.x = x;
		camera.y = y;
		camera.z = z;
	}
	
	public void add( Node n )
	{
		root.add( n );
	}

	public void setFocus( float x, float y, float z )
	{
		focus.set( x, y, z );
	}
}
