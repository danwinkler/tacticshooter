package tacticshooter.scenegraph;

import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.LinkedList;


import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

import com.phyloa.dlib.graphics.Transformable;

public class Node extends Transformable
{
	Model model;
	
	LinkedList<Node> children = new LinkedList<Node>();
	
	boolean remove = false;
	
	private Texture toBind;
	
	public Node()
	{
		
	}
	
	public void render()
	{
		GL11.glPushMatrix();
		
		FloatBuffer fb = BufferUtils.createFloatBuffer( 16 );
		fb.put( new float[] { mat.m00, mat.m10, mat.m20, mat.m30,
							mat.m01, mat.m11, mat.m21, mat.m31,
							mat.m02, mat.m12, mat.m22, mat.m32,
							mat.m03, mat.m13, mat.m23, mat.m33 } );
		fb.flip();
		//GL11.glMultMatrix( fb );
		if( model != null )
		{
			if( getTexture() != null ) getTexture().bind();
			model.render();
		}
		Iterator<Node> i = children.iterator();
		while( i.hasNext() )
		{
			Node n = i.next();
			n.render();
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
}
