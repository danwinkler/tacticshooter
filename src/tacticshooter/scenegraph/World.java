package tacticshooter.scenegraph;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBDepthTexture;
import org.lwjgl.opengl.ARBShadow;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
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
	
	public ShaderProgram shader;
	
	ArrayList<ShaderNodeOp> snos = new ArrayList<ShaderNodeOp>();
	
	boolean shaderEnabled;
	boolean shadows = true;
	
	float transparency = 1;
	
	boolean shadowsSetUp = false;
	int shadowMapTexture;
	Matrix4f biasMatrix = new Matrix4f( 0.5f, 0.0f, 0.0f, 0.0f,
										0.0f, 0.5f, 0.0f, 0.0f,
										0.0f, 0.0f, 0.5f, 0.0f,
										0.5f, 0.5f, 0.5f, 1.0f);
	
	Matrix4f lightProjectionMatrix = new Matrix4f();
	Matrix4f lightViewMatrix = new Matrix4f();
	
	FloatBuffer row = BufferUtils.createFloatBuffer( 4 );
	
	public void setUpCamera()
	{
		GLU.gluLookAt( camera.x, camera.y, camera.z, focus.x, focus.y, focus.z, up.x, up.y, up.z );
		
		if( shadows && !shadowsSetUp && lights.size() >= 2 )
		{
			//Create the shadow map texture
			shadowMapTexture = GL11.glGenTextures();
			GL11.glBindTexture( GL11.GL_TEXTURE_2D, shadowMapTexture);
			GL11.glTexImage2D( GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, 1024, 1024, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null );
			GL11.glTexParameteri( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST );
			GL11.glTexParameteri( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST );
			GL11.glTexParameteri( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP );
			GL11.glTexParameteri( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP );
			
			FloatBuffer buf = BufferUtils.createFloatBuffer( 16 );
			float[] arr = new float[16];
			Light l = lights.get( 0 );
			
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			GLU.gluPerspective(45.0f, 1.0f, 2.0f, 8.0f);
			GL11.glGetFloat( GL11.GL_MODELVIEW_MATRIX, buf );
			buf.get( arr );
			
			lightProjectionMatrix.set( arr );
			buf.rewind();
			
			GL11.glLoadIdentity();
			GLU.gluLookAt( l.positionArr[0], l.positionArr[1], l.positionArr[2],
						focus.x, focus.y, focus.z,
						up.x, up.y, up.z );
			GL11.glGetFloat( GL11.GL_MODELVIEW_MATRIX, buf );
			buf.get( arr );
			
			lightViewMatrix.set( arr );
			
			GL11.glPopMatrix();
			
			shadowsSetUp = true;
		}
	}
	
	public void render( int width, int height )
	{
		GL11.glPushMatrix();
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
		
		if( shadows && lights.size() >= 2 )
		{
			Light l = lights.get( 0 );
			
			//Draw from lights pov
			GL11.glMatrixMode( GL11.GL_PROJECTION );
			GL11.glLoadIdentity();
			GLU.gluPerspective(45.0f, 1.0f, 2.0f, 8.0f);
			
			GL11.glMatrixMode( GL11.GL_MODELVIEW );
			GL11.glLoadIdentity();
			GLU.gluLookAt( l.positionArr[0], l.positionArr[1], l.positionArr[2],
						focus.x, focus.y, focus.z,
						up.x, up.y, up.z );
			
			GL11.glViewport( 0, 0, 1024, 1024 );
			GL11.glCullFace( GL11.GL_FRONT );
			GL11.glShadeModel( GL11.GL_FLAT );
			GL11.glColorMask( false, false, false, false );
			
			root.render( this );
			
			GL11.glBindTexture( GL11.GL_TEXTURE_2D, shadowMapTexture );
			GL11.glCopyTexSubImage2D( GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, 1024, 1024 );

			//restore states
			GL11.glCullFace( GL11.GL_BACK );
			GL11.glShadeModel( GL11.GL_SMOOTH );
			GL11.glColorMask( true, true, true, true );
			
			GL11.glViewport( 0, 0, width, height );
			
			setUpCamera();
			
			//Use dim light to represent shadowed areas
			lights.get( 1 ).enable( 0 );

			root.render( this );
			
			lights.get( 1 ).disable( 0 );
			
			//3rd pass
			//Draw with bright light
			lights.get( 0 ).enable( 0 );
			
			//Calculate texture matrix for projection
			//This matrix takes us from eye space to the light's clip space
			//It is postmultiplied by the inverse of the current view matrix when specifying texgen
			Matrix4f textureMatrix = new Matrix4f( biasMatrix );
			textureMatrix.mul( lightProjectionMatrix );
			textureMatrix.mul( lightViewMatrix );
			
			float[] rowArr = new float[4];
			
			textureMatrix.getRow( 0, rowArr );
			row.put( rowArr );
			//Set up texture coordinate generation.
			GL11.glTexGeni( GL11.GL_S, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_EYE_LINEAR );
			GL11.glTexGen( GL11.GL_S, GL11.GL_EYE_PLANE, row );
			GL11.glEnable( GL11.GL_TEXTURE_GEN_S );

			textureMatrix.getRow( 1, rowArr );
			row.put( rowArr );
			
			GL11.glTexGeni( GL11.GL_T, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_EYE_LINEAR);
			GL11.glTexGen( GL11.GL_T, GL11.GL_EYE_PLANE, row );
			GL11.glEnable( GL11.GL_TEXTURE_GEN_T );

			textureMatrix.getRow( 2, rowArr );
			row.put( rowArr );
			
			GL11.glTexGeni( GL11.GL_R, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_EYE_LINEAR);
			GL11.glTexGen( GL11.GL_R, GL11.GL_EYE_PLANE, row );
			GL11.glEnable( GL11.GL_TEXTURE_GEN_R );

			textureMatrix.getRow( 3, rowArr );
			row.put( rowArr );
			
			GL11.glTexGeni( GL11.GL_Q, GL11.GL_TEXTURE_GEN_MODE, GL11.GL_EYE_LINEAR );
			GL11.glTexGen( GL11.GL_Q, GL11.GL_EYE_PLANE, row );
			GL11.glEnable( GL11.GL_TEXTURE_GEN_Q );

			//Bind & enable shadow map texture
			GL11.glBindTexture( GL11.GL_TEXTURE_2D, shadowMapTexture );
			GL11.glEnable( GL11.GL_TEXTURE_2D );

			//Enable shadow comparison
			GL11.glTexParameteri( GL11.GL_TEXTURE_2D, ARBShadow.GL_TEXTURE_COMPARE_MODE_ARB, GL14.GL_COMPARE_R_TO_TEXTURE );

			//Shadow comparison should be true (ie not in shadow) if r<=texture
			GL11.glTexParameteri( GL11.GL_TEXTURE_2D, ARBShadow.GL_TEXTURE_COMPARE_FUNC_ARB, GL11.GL_LEQUAL);

			//Shadow comparison should generate an INTENSITY result
			GL11.glTexParameteri( GL11.GL_TEXTURE_2D, ARBDepthTexture.GL_DEPTH_TEXTURE_MODE_ARB, GL11.GL_INTENSITY);

			//Set alpha test to discard false comparisons
			GL11.glAlphaFunc( GL11.GL_GEQUAL, 0.99f );
			GL11.glEnable( GL11.GL_ALPHA_TEST );

			root.render( this );
			
			lights.get( 0 ).disable( 0 );
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
		GL11.glPopMatrix();
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

	public Point3f getCamera()
	{
		return camera;
	}

	public boolean isPlainRender()
	{
		return plainRender;
	}

	public ShaderProgram getShader()
	{
		return shader;
	}

	public float getTransparency()
	{
		return transparency;
	}

	public void setTransparency( float transparency )
	{
		this.transparency = transparency;
	}
}
