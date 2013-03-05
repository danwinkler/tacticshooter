package tacticshooter.scenegraph;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;


import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;

import com.phyloa.dlib.graphics.Transformable;

public class Node extends Transformable
{
	Model model;
	
	Point3f position = new Point3f( 0, 0, 0 );
	Vector3f scale = new Vector3f( 1, 1, 1 );
	
	Color color = Color.white;
	
	LinkedList<Node> children = new LinkedList<Node>();
	
	boolean remove = false;
	
	private Texture toBind;
	
	boolean visible = true;
	
	FloatBuffer fb;
	float[] fba = new float[16];
	
	public Node()
	{
		
	}
	
	public void render( World world )
	{
		if( !visible ) return;
		
		GL11.glPushMatrix();
		GL11.glTranslatef( position.x, position.y, position.z );
		if( fb == null )
		{
			fb = BufferUtils.createFloatBuffer( 16 );
		}
		
		fba[0] = mat.m00;
		fba[1] = mat.m10;
		fba[2] = mat.m20;
		fba[3] = mat.m30;
		fba[4] = mat.m01;
		fba[5] = mat.m11;
		fba[6] = mat.m21;
		fba[7] = mat.m31;
		fba[8] = mat.m02;
		fba[9] = mat.m12;
		fba[10] = mat.m22;
		fba[11] = mat.m32;
		fba[12] = mat.m03;
		fba[13] = mat.m13;
		fba[14] = mat.m23;
		fba[15] = mat.m33;
		
		fb.put( fba );
		fb.flip();
		
		
		GL11.glMultMatrix( fb );
		GL11.glScalef( scale.x, scale.y, scale.z );	
		if( model != null )
		{
			if( !world.plainRender )
			{
				GL11.glColor4f( color.r, color.g, color.b, world.transparency );
				if( toBind != null )
				{	
					toBind.bind();
					GL11.glEnable( GL11.GL_TEXTURE_2D );
				}
				else
				{
					GL11.glDisable( GL11.GL_TEXTURE_2D );
				}
				
				if( world.shaderEnabled )
				{
					world.executeSNOs( this );
				}
			}
			model.render();
		}
		Iterator<Node> i = children.iterator();
		while( i.hasNext() )
		{
			Node n = i.next();
			n.render( world );
			if( n.remove )
			{
				i.remove();
			}
		}
		GL11.glPopMatrix();
	}

	public void add( Node n )
	{
		children.add( n );
	}

	public void setModel( Model model )
	{
		this.model = model;
	}
	
	public Model getModel()
	{
		return model;
	}
	
	public void markForDeletion()
	{
		remove = true;
	}

	public Texture getTexture()
	{
		return toBind;
	}

	public void setTexture( Texture texture )
	{
		this.toBind = texture;
	}
	
	public void setPosition( float x, float y, float z )
	{
		position.set( x, y, z );
	}
	
	public void setScale( float x, float y, float z )
	{
		scale.set( x, y, z );
	}

	public void setColor( Color c )
	{
		this.color = c;
	}

	public LinkedList<Node> getChildren()
	{
		return children;
	}
	
	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible( boolean visible )
	{
		this.visible = visible;
	}

	public Point3f getPosition()
	{
		return position;
	}

}
