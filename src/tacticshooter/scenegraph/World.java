package tacticshooter.scenegraph;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBDepthTexture;
import org.lwjgl.opengl.ARBShadow;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.newdawn.slick.opengl.shader.ShaderProgram;

import static org.lwjgl.opengl.ARBShadowAmbient.GL_TEXTURE_COMPARE_FAIL_VALUE_ARB;
import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.util.glu.GLU.gluLookAt;
import static org.lwjgl.util.glu.GLU.gluPerspective;

public class World
{
	Node root = new Node();
	
	Point3f camera = new Point3f();
	Point3f focus = new Point3f();
	Vector3f up = new Vector3f( 0, 0, -1 );
	
	ArrayList<Light> lights = new ArrayList<Light>();
	boolean lightsEnabled = false;

	boolean plainRender = false;
	
	public ShaderProgram shader;
	
	ArrayList<ShaderNodeOp> snos = new ArrayList<ShaderNodeOp>();
	
	boolean shaderEnabled;
	
	float transparency = 1;
	
	boolean shadows = false;
	boolean shadowsSetUp = false;
	
	 // This represents if the clients computer has the ambient shadow extention
    private static boolean ambientShadowsAvailable;
    // Enable this if you want to see the depth texture for debugging purposes.
    private static boolean showShadowMap = false;
	private static boolean useFBO = false;
	private static int shadowWidth = 1024;
    private static int shadowHeight = 1024;

    private static int frameBuffer;
    private static int renderBuffer;

    private static final FloatBuffer ambientLight = BufferUtils.createFloatBuffer(4);
    private static final FloatBuffer diffuseLight = BufferUtils.createFloatBuffer(4);
    private static final FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
    private static final FloatBuffer tempBuffer = BufferUtils.createFloatBuffer(4);

    private static final Matrix4f textureMatrix = new Matrix4f();
	
	FloatBuffer row = BufferUtils.createFloatBuffer( 4 );
	
	public void setUpCamera()
	{
		GLU.gluLookAt( camera.x, camera.y, camera.z, focus.x, focus.y, focus.z, up.x, up.y, up.z );
		
		if( shadows )
		{
			setUpBufferValues();
			
			glEnable(GL_DEPTH_TEST);
	        glDepthFunc(GL_LEQUAL);
	        glPolygonOffset( 4.0f, 0.0F);

	        glShadeModel(GL_SMOOTH);
	        glEnable(GL_LIGHTING);
	        glEnable(GL_COLOR_MATERIAL);
	        glEnable(GL_NORMALIZE);
	        glEnable(GL_LIGHT0);

	        // Setup some texture states
	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	        glTexParameteri(GL_TEXTURE_2D, GL_DEPTH_TEXTURE_MODE, GL_INTENSITY);

	        // If ambient shadows are availible then we can skip a rendering pass.
	        if (ambientShadowsAvailable) {
	            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FAIL_VALUE_ARB, 0.5F);
	        }

	        glTexGeni(GL_S, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
	        glTexGeni(GL_T, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
	        glTexGeni(GL_R, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
	        glTexGeni(GL_Q, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);

	        // If we are using a FBO, we need to setup the framebuffer.
	        if (useFBO) {
	            frameBuffer = glGenFramebuffersEXT();
	            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, frameBuffer);

	            renderBuffer = glGenRenderbuffersEXT();
	            glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, renderBuffer);

	            glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH_COMPONENT32, 8192, 8192);

	            glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT,
	                    renderBuffer);

	            glDrawBuffer(GL_NONE);
	            glReadBuffer(GL_NONE);

	            int FBOStatus = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);
	            if (FBOStatus != GL_FRAMEBUFFER_COMPLETE_EXT) {
	                System.out.println("Framebuffer error!");
	            }

	            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
	        }
			
			if( GLContext.getCapabilities().GL_ARB_shadow_ambient ) 
			{
	            ambientShadowsAvailable = true;
			}
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
			setUpBufferValues();
			generateShadowMap();
			 glMatrixMode(GL_PROJECTION);
		        glLoadIdentity();
		        gluPerspective(40, (float) Display.getWidth() / (float) Display.getHeight(), 5.0F, 10000.0F);
		        glMatrixMode(GL_MODELVIEW);
		        glLoadIdentity();
		        
		        
		        setUpCamera();
		        
		        glViewport( 0, 0, Display.getWidth(), Display.getHeight() );

		        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
		        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		        if ( showShadowMap ) {
		            glMatrixMode(GL_PROJECTION);
		            glLoadIdentity();
		            glMatrixMode(GL_MODELVIEW);
		            glLoadIdentity();
		            glMatrixMode(GL_TEXTURE);
		            glPushMatrix();
		            glLoadIdentity();
		            glEnable(GL_TEXTURE_2D);
		            glDisable(GL_LIGHTING);
		            glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
		            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);

		            glBegin(GL_QUADS);
		            glTexCoord2f(0.0F, 0.0F);
		            glVertex2f(-1.0F, -1.0F);
		            glTexCoord2f(1.0F, 0.0F);
		            glVertex2f(1.0F, -1.0F);
		            glTexCoord2f(1.0F, 1.0F);
		            glVertex2f(1.0F, 1.0F);
		            glTexCoord2f(0.0F, 1.0F);
		            glVertex2f(-1.0F, 1.0F);
		            glEnd();

		            glDisable(GL_TEXTURE_2D);
		            glEnable(GL_LIGHTING);
		            glPopMatrix();
		            glMatrixMode(GL_PROJECTION);
		            gluPerspective(45.0F, 1.0F, 1.0F, 1000.0F);
		            glMatrixMode(GL_MODELVIEW);
		        } else {
		            /*
		                    * If we dont have the ambient shadow extention, we will need to
		                    * add an extra rendering pass.
		                    */
		            if ( !ambientShadowsAvailable ) {
		                FloatBuffer lowAmbient = BufferUtils.createFloatBuffer(4);
		                lowAmbient.put(new float[]{0.1F, 0.1F, 0.1F, 1.0F});
		                lowAmbient.flip();

		                FloatBuffer lowDiffuse = BufferUtils.createFloatBuffer(4);
		                lowDiffuse.put(new float[]{0.35F, 0.35F, 0.35F, 1.0F});
		                lowDiffuse.flip();

		                glLight(GL_LIGHT0, GL_AMBIENT, lowAmbient);
		                glLight(GL_LIGHT0, GL_DIFFUSE, lowDiffuse);

		                plainRender = true;
			            root.render( this );
			            plainRender = false;

		                glAlphaFunc(GL_GREATER, 0.9F);
		                glEnable(GL_ALPHA_TEST);
		            }

		            glLight(GL_LIGHT0, GL_AMBIENT, ambientLight);
		            glLight(GL_LIGHT0, GL_DIFFUSE, diffuseLight);

		            glEnable(GL_TEXTURE_2D);
		            glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
		            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE);

		            glEnable(GL_TEXTURE_GEN_S);
		            glEnable(GL_TEXTURE_GEN_T);
		            glEnable(GL_TEXTURE_GEN_R);
		            glEnable(GL_TEXTURE_GEN_Q);

		            tempBuffer.put(0, textureMatrix.m00);
		            tempBuffer.put(1, textureMatrix.m01);
		            tempBuffer.put(2, textureMatrix.m02);
		            tempBuffer.put(3, textureMatrix.m03);

		            glTexGen(GL_S, GL_EYE_PLANE, tempBuffer);

		            tempBuffer.put(0, textureMatrix.m10);
		            tempBuffer.put(1, textureMatrix.m11);
		            tempBuffer.put(2, textureMatrix.m12);
		            tempBuffer.put(3, textureMatrix.m13);

		            glTexGen(GL_T, GL_EYE_PLANE, tempBuffer);

		            tempBuffer.put(0, textureMatrix.m20);
		            tempBuffer.put(1, textureMatrix.m21);
		            tempBuffer.put(2, textureMatrix.m22);
		            tempBuffer.put(3, textureMatrix.m23);

		            glTexGen(GL_R, GL_EYE_PLANE, tempBuffer);

		            tempBuffer.put(0, textureMatrix.m30);
		            tempBuffer.put(1, textureMatrix.m31);
		            tempBuffer.put(2, textureMatrix.m32);
		            tempBuffer.put(3, textureMatrix.m33);

		            glTexGen(GL_Q, GL_EYE_PLANE, tempBuffer);
		            
		            plainRender = true;
		            root.render( this );
		            plainRender = false;
		            
		            glDisable(GL_ALPHA_TEST);
		            glDisable(GL_TEXTURE_2D);
		            glDisable(GL_TEXTURE_GEN_S);
		            glDisable(GL_TEXTURE_GEN_T);
		            glDisable(GL_TEXTURE_GEN_R);
		            glDisable(GL_TEXTURE_GEN_Q);
		        }

		        if (glGetError() != GL_NO_ERROR) {
		            System.out.println("An OpenGL error occurred");
		        }
		}
		else
		{
			root.render( this );
		}
			
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
	
	 private void generateShadowMap() 
	 {
		 float lightToSceneDistance, nearPlane, fieldOfView;
		 FloatBuffer lightModelView = BufferUtils.createFloatBuffer(16);
		 FloatBuffer lightProjection = BufferUtils.createFloatBuffer(16);
		 Matrix4f lightProjectionTemp = new Matrix4f();
		 Matrix4f lightModelViewTemp = new Matrix4f();
		 
		 float sceneBoundingRadius = 250.0F;
		
		lightToSceneDistance = (float) Math.sqrt(lightPosition.get(0) * lightPosition.get(0) + lightPosition.get(1) *
		        lightPosition.get(1) + lightPosition.get(2) * lightPosition.get(2));
		
		nearPlane = lightToSceneDistance - sceneBoundingRadius;
		
		fieldOfView = (float) Math.toDegrees(2.0F * Math.atan(sceneBoundingRadius / lightToSceneDistance));
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho( focus.x - 100, focus.x + 100, focus.y + 100, focus.x - 100, -100, 100 );
		glGetFloat(GL_PROJECTION_MATRIX, lightProjection);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		gluLookAt(lightPosition.get(0), lightPosition.get(1), lightPosition.get(2), focus.x, focus.y, 0.0F, 0.0F, 0.0F, -1.0F);
		glGetFloat(GL_MODELVIEW_MATRIX, lightModelView);
		glViewport(0, 0, shadowWidth, shadowHeight);
		
		if (useFBO) {
		    glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, frameBuffer);
		}
	
	        glClear(GL_DEPTH_BUFFER_BIT);
	
	        // Set rendering states to the minimum required, for speed.
	glShadeModel(GL_FLAT);
	glDisable(GL_LIGHTING);
	glDisable(GL_COLOR_MATERIAL);
	glDisable(GL_NORMALIZE);
	glColorMask(false, false, false, false);
	
	glEnable(GL_POLYGON_OFFSET_FILL);
	
	plainRender = true;
	root.render( this );
	plainRender = false;
	
	glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, 0, 0, shadowWidth, shadowHeight, 0);
	
	// Unbind the framebuffer if we are using them.
	if (useFBO) {
	    glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
	}
	
	// Setup the rendering states.
	    glShadeModel(GL_SMOOTH);
	    glEnable(GL_LIGHTING);
	    glEnable(GL_COLOR_MATERIAL);
	    glEnable(GL_NORMALIZE);
	    glColorMask(true, true, true, true);
	    glDisable(GL_POLYGON_OFFSET_FILL);
	
	    lightProjectionTemp.load(lightProjection);
	    lightModelViewTemp.load(lightModelView);
	    lightProjection.flip();
	    lightModelView.flip();
	
	    Matrix4f tempMatrix = new Matrix4f();
	    tempMatrix.setIdentity();
	    tempMatrix.translate( new org.lwjgl.util.vector.Vector3f( 0.5F, 0.5F, 0.5F ) );
	    tempMatrix.scale( new org.lwjgl.util.vector.Vector3f( .5F, 0.5F, 0.5F ) );
	    Matrix4f.mul(tempMatrix, lightProjectionTemp, textureMatrix);
	    Matrix4f.mul(textureMatrix, lightModelViewTemp, tempMatrix);
	    Matrix4f.transpose(tempMatrix, textureMatrix);
	}
	 
	 private void setUpBufferValues() 
	 {
		ambientLight.put(new float[]{0.2F, 0.2F, 0.2F, 1.0F});
		ambientLight.flip();
		
		diffuseLight.put(new float[]{0.7F, 0.7F, 0.7F, 1.0F});
		diffuseLight.flip();
		
		lightPosition.put(new float[]{ focus.x - 200, focus.y - 200, -200, 0.0F});
		lightPosition.flip();
	 }
}
