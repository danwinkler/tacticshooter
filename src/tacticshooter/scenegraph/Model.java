package tacticshooter.scenegraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;


import com.phyloa.dlib.graphics.Transformable;

public class Model extends Transformable
{
	int callList;
	
	Vector3f scale = new Vector3f( 1, 1, 1 );
	
	ArrayList<Triangle> triangles = new ArrayList<Triangle>();
	
	FloatBuffer fb = null;
	
	public Model()
	{
		callList = GL11.glGenLists( 1 );
	}
	
	public Model( String model )
	{
		this();
		try
		{
			ModelHelpers.loadModel( model, this );
		} catch( FileNotFoundException e )
		{
			e.printStackTrace();
		}
		writeToCallList();
	}
	
	public void writeToCallList()
	{
		begin();
		GL11.glBegin( GL11.GL_TRIANGLES );
		for( Triangle t : triangles )
		{
			for( int i = 0; i < 3; i++ )
			{
				Vector3f n = t.normals[i];
				Point3f p = t.points[i];
				Vector2f tc = t.texCoords[i];
				
				GL11.glNormal3f( n.x, n.y, n.z );
				GL11.glTexCoord2f( tc.x, tc.y );
				GL11.glVertex3f( p.x, p.y, p.z );
			}
		}
		GL11.glEnd();
		end();
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
		GL11.glPushMatrix();
		if( fb == null )
		{
			fb = BufferUtils.createFloatBuffer( 16 );
			fb.put( new float[] { mat.m00, mat.m10, mat.m20, mat.m30,
					mat.m01, mat.m11, mat.m21, mat.m31,
					mat.m02, mat.m12, mat.m22, mat.m32,
					mat.m03, mat.m13, mat.m23, mat.m33 } );
			fb.flip();
		}
		
		GL11.glMultMatrix( fb );
		GL11.glScalef( scale.x, scale.y, scale.z );	
		GL11.glCallList( callList );
		GL11.glPopMatrix();
	}
	
	public void setScale( float x, float y, float z )
	{
		scale.set( x, y, z );
	}
}
