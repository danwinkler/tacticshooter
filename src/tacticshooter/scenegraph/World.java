package tacticshooter.scenegraph;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.opengl.shader.ShaderProgram;

public class World
{
	Node root = new Node();
	
	Point3f camera = new Point3f();
	Point3f focus = new Point3f();
	Vector3f up = new Vector3f( 0, 0, -1 );
	
	ArrayList<Light> lights = new ArrayList<Light>();
	boolean lightsEnabled = false;

	boolean plainRender;
	
	ShaderProgram shader;
	
	ArrayList<ShaderNodeOp> snos = new ArrayList<ShaderNodeOp>();
	
	boolean shaderEnabled;
	
	public void setUpCamera()
	{
		GLU.gluLookAt( camera.x, camera.y, camera.z, focus.x, focus.y, focus.z, up.x, up.y, up.z );
	}
	
	public void render()
	{
		if( lightsEnabled && !plainRender )
		{
			GL11.glEnable( GL11.GL_LIGHTING );
			GL11.glLightModeli( GL11.GL_LIGHT_MODEL_TWO_SIDE, GL11.GL_TRUE );
			GL11.glEnable( GL11.GL_COLOR_MATERIAL );
			GL11.glColorMaterial( GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE );
			   
			for( int i = 0; i < lights.size(); i++ )
			{
				Light l = lights.get( i );
				l.enable( i );
			}
		}
		
		if( shaderEnabled && !plainRender )
		{
			shader.bind();
		}
		
		root.render( this );
		
		if( shaderEnabled && !plainRender )
		{
			shader.unbind();
		}
		
		if( lightsEnabled && !plainRender )
		{
			for( int i = 0; i < lights.size(); i++ )
			{
				lights.get( i ).disable( i );
			}
			GL11.glDisable( GL11.GL_LIGHTING );
		}
	}
	
	public void setShader( ShaderProgram shader )
	{
		this.shader = shader;
	}
	
	public void setShaderEnabled( boolean enabled )
	{
		this.shaderEnabled = enabled;
	}
	
	public void addShaderNodeOp( ShaderNodeOp sno )
	{
		snos.add( sno );
	}
	
	public void setTextureEnabled( boolean enabled )
	{
		if( enabled )
		{
			GL11.glEnable( GL11.GL_TEXTURE_2D );
		}
		else
		{
			GL11.glDisable( GL11.GL_TEXTURE_2D );
		}
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
	
	public void setLightsEnabled( boolean enabled )
	{
		this.lightsEnabled = enabled;
	}
	
	public void setPlainRender( boolean enabled )
	{
		this.plainRender = enabled;
	}

	public void add( Light light )
	{
		lights.add( light );
	}

	public void executeSNOs( Node node )
	{
		for( ShaderNodeOp sno : snos )
		{
			sno.execute( shader, node );
		}
	}
}
