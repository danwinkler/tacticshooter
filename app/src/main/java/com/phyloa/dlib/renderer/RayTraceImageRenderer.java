package com.phyloa.dlib.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import jp.objectclub.vecmath.AxisAngle4f;
import jp.objectclub.vecmath.Matrix3f;
import jp.objectclub.vecmath.Matrix4f;
import jp.objectclub.vecmath.Point2f;
import jp.objectclub.vecmath.Point3f;
import jp.objectclub.vecmath.Vector2f;
import jp.objectclub.vecmath.Vector3f;

import com.phyloa.dlib.graphics.Transformable;
import com.phyloa.dlib.math.Boxf;
import com.phyloa.dlib.math.Geom;
import com.phyloa.dlib.math.GeomOctTree;
import com.phyloa.dlib.math.Intersection;
import com.phyloa.dlib.math.Rayf;
import com.phyloa.dlib.math.Trianglef;
import com.phyloa.dlib.util.DGraphics;

public class RayTraceImageRenderer extends Transformable implements IRenderer
{
	public GeomOctTree oct = new GeomOctTree();
	ArrayList<Point3f> vertexBuffer = new ArrayList<Point3f>();
	ArrayList<Light> lights = new ArrayList<Light>();
	ArrayList<Geom> geom = new ArrayList<Geom>();
	
	ShapeType mode;
	BufferedImage im;
	Point3f[][] values;
	float maxValue = 0;
	int done = 0;
	
	int width;
	int height;
	
	int color;
	
	public boolean finished = false;
	
	Point3f backgroundColor = new Point3f();
	
	Point3f cameraLoc = new Point3f( 0, 0, 0 );
	Vector3f cameraLook = new Vector3f();
	Vector3f cameraUp = new Vector3f();
	Matrix3f camera = new Matrix3f();
	float viewAngleY = 30.f;
	float viewAngleX;
	float lift = (float) Math.tan( Math.toRadians( viewAngleY ) );
	float breadth = (float) Math.tan( Math.toRadians( viewAngleX ) );
	
	boolean scaleLight = false;
	int cBoxSize = 50;
	
	public RayTraceImageRenderer( int x, int y )
	{
		this.width = x;
		this.height = y;

		viewAngleX = (viewAngleY/y) * x;
		
		lift = (float) Math.tan( Math.toRadians( viewAngleY ) );
		breadth = (float) Math.tan( Math.toRadians( viewAngleX ) );
		

		
		im = new BufferedImage( x, y, BufferedImage.TYPE_INT_ARGB );
		values = new Point3f[x][y];
	}
	
	int threadnum = 8;
	
	public void begin()
	{
		for( threadnum = 8; threadnum <= width; threadnum++ )
		{
			if( width % threadnum == 0 )
				break;
		}
		int chunkwidth = width/threadnum;
		for( int i = 0; i < threadnum; i++ )
		{
			TracerThread t = new TracerThread( i*chunkwidth, chunkwidth );
			new Thread( t ).start();
		}
		new Thread( new Runnable() {

			@Override
			public void run()
			{
				while( done < threadnum )
				{
					try
					{
						Thread.sleep( 100 );
					} catch( InterruptedException e )
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for( int x = 0; x < width; x++ )
				{
					for( int y = 0; y < height; y++ )
					{
						if( scaleLight )
						{
							values[x][y].x = (values[x][y].x / maxValue) * 255;
							values[x][y].y = (values[x][y].y / maxValue) * 255;
							values[x][y].z = (values[x][y].z / maxValue) * 255;
						}
						else
						{
							values[x][y].x = Math.min( values[x][y].x, 255 );
							values[x][y].y = Math.min( values[x][y].y, 255 );
							values[x][y].z = Math.min( values[x][y].z, 255 );
						}
						im.setRGB( x, y, DGraphics.rgb( (int)values[x][y].x, (int)values[x][y].y, (int)values[x][y].z ) );
					}
				}
				finished = true;
			} } ).start();
	}
	
	public class TracerThread implements Runnable
	{
		int x1;
		int x2;
		public TracerThread()
		{
			
		}
		
		public TracerThread( int i, int j ) 
		{
			x1 = i;
			x2 = j;
		}

		public void run() 
		{
			for( int i = x1; i < x1+x2; i++ )
			{
				for( int y = 0; y < height; y++ )
				{
					Rayf ray = new Rayf( cameraLoc, getLookVector( i, y ) );
					Point3f col = trace( ray );
					if( col.x > maxValue ) maxValue = col.x;
					if( col.y > maxValue ) maxValue = col.y;
					if( col.z > maxValue ) maxValue = col.z;
					synchronized( values )
					{
						im.setRGB( i, y, DGraphics.rgb( (int)Math.min( 255, col.x ), (int)Math.min( 255, col.y ), (int)Math.min( 255, col.z ) ) );
						values[i][y] = col;
					}
				}
			}
			done++;
		}
		
		public Point3f trace( Rayf ray )
		{
			//For all triangles
			Intersection point = collideWithObject( ray );
			if( point != null )
			{
				//If object is found
				int col = point.getGeom().getColor(0,0);
				Point3f color = new Point3f( DGraphics.getRed( col ), DGraphics.getGreen( col ), DGraphics.getBlue( col ) );
				Point3f rcolor = new Point3f();
				for( int i = 0; i < lights.size(); i++ )
				{
					Vector3f lightVec = new Vector3f( lights.get(i).loc );
					lightVec.sub( point.getLoc() );
					Intersection light = collideWithObject( new Rayf( point.getLoc(), lightVec ) );
					//float dist2 = (float)(1.f / Math.pow( lightVec.lengthSquared(), 1.f / lights.get( i ).b ));
					float dist2 = (float)(1.f / ( lightVec.lengthSquared())) * lights.get( i ).b;
					if( light == null )
					{
						rcolor.x += color.x * dist2;
						rcolor.y += color.y * dist2;
						rcolor.z += color.z * dist2;
					}
					else 
					{
						rcolor.x += color.x * dist2 * .5f;
						rcolor.y += color.y * dist2 * .5f;
						rcolor.z += color.z * dist2 * .5f;
					}
				}
				return rcolor;
			}
			else 
				return backgroundColor;
		}
	}
	
	public Intersection collideWithObject( Rayf ray )
	{
		Intersection point = null;
		
		for( int j = 0; j < geom.size(); j++ )
		{
			Intersection temp = geom.get(j).intersects( ray );
			if( point != null && temp != null )
			{
				if( temp.getDist() < point.getDist() )
					point = temp;
			}
			else if( point == null )
			{
				point = temp;
			}
		}
		
		//point = oct.closestIntersect( ray );
		return point;
	}
	
	public Vector3f getLookVector( int x, int y )
	{
		float xNorm = (float)x / (float)width;
		float yNorm = (float)y / (float)height;
		Vector3f vec = new Vector3f( (breadth * xNorm) - (breadth/2), 1, (lift * yNorm) - (lift/2) );
		camera.transform( vec );
		return vec;
	}
	
	public BufferedImage getImage()
	{
		return im;
	}
	
	public void setCamera( float cx, float cy, float cz, float lx, float ly, float lz, float ux, float uy, float uz )
	{
		cameraLoc.x = cx;
		cameraLoc.y = cy;
		cameraLoc.z = cz;
		cameraLook.x = lx;
		cameraLook.y = ly;
		cameraLook.z = lz;
		cameraUp.x = ux;
		cameraUp.y = uy;
		cameraUp.z = uz;
		cameraUp.scale( -1 );
		transform( cameraLoc );
		Vector3f cameraRight = new Vector3f();
		cameraRight.cross( cameraLook, cameraUp );
		
		cameraUp.normalize();
		cameraLook.normalize();
		cameraRight.normalize();
		camera.setIdentity();
		camera.m00 = cameraRight.x;
		camera.m10 = cameraRight.y;
		camera.m20 = cameraRight.z;
		camera.m01 = cameraLook.x;
		camera.m11 = cameraLook.y;
		camera.m21 = cameraLook.z;
		camera.m02 = cameraUp.x;
		camera.m12 = cameraUp.y;
		camera.m22 = cameraUp.z;
	}
	
	public void vertex( float x, float y )
	{
		Point3f v = new Point3f( x, y, 0 );
		transform( v );
		vertexBuffer.add( v );
	}
	
	public void vertex( float x, float y, float z )
	{
		Point3f v = new Point3f( x, y, z );
		transform( v );
		vertexBuffer.add( v );
	}

	public void beginShape( ShapeType type )
	{
		this.mode = type;
		vertexBuffer.clear();
	}

	public void box( float width, float height, float length )
	{
		beginShape( ShapeType.QUADS );
		float x2 = width/2;
		float y2 = height/2;
		float z2 = length/2;
		//TOP
		vertex( x2, y2, z2 );
		vertex( x2, -y2, z2 );
		vertex( -x2, -y2, z2 );
		vertex( -x2, y2, z2 );
		//BOTTOM
		vertex( x2, y2, -z2 );
		vertex( x2, -y2, -z2 );
		vertex( -x2, -y2, -z2 );
		vertex( -x2, y2, -z2 );
		//FRONT
		vertex( x2, y2, z2 );
		vertex( x2, y2, -z2 );
		vertex( -x2, y2, -z2 );
		vertex( -x2, y2, z2 );
		//BACK
		vertex( x2, -y2, z2 );
		vertex( x2, -y2, -z2 );
		vertex( -x2, -y2, -z2 );
		vertex( -x2, -y2, z2 );
		//LEFT
		vertex( x2, y2, z2 );
		vertex( x2, y2, -z2 );
		vertex( x2, -y2, -z2 );
		vertex( x2, -y2, z2 );
		//RIGHT
		vertex( -x2, y2, z2 );
		vertex( -x2, y2, -z2 );
		vertex( -x2, -y2, -z2 );
		vertex( -x2, -y2, z2 );
		endShape();
	}

	public void endShape()
	{
		switch( mode )
		{
		case TRIANGLES:
			for( int i = 0; i < vertexBuffer.size(); i += 3 )
			{
				addTriangle( vertexBuffer.get(i), vertexBuffer.get(i+1), vertexBuffer.get(i+2), color );
			}
		break;
		case QUADS:
			for( int i = 0; i < vertexBuffer.size(); i += 4 )
			{
				addTriangle( vertexBuffer.get(i), vertexBuffer.get(i+1), vertexBuffer.get(i+2), color );
				addTriangle( vertexBuffer.get(i), vertexBuffer.get(i+3), vertexBuffer.get(i+2), color );
			}
		break;
		}
		
		vertexBuffer.clear();
	}
	
	public void addTriangle( Point3f p1, Point3f p2, Point3f p3, int color )
	{
		Trianglef t = new Trianglef( p1, p2, p3, color );
		oct.addGeom( t );
		geom.add( t );
	}
	
	public void addTriangle( Point3f p1, Point3f p2, Point3f p3 )
	{
		Trianglef t = new Trianglef( p1, p2, p3 );
		oct.addGeom( t );
		geom.add( t );
	}
	
	public void addTriangle( float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3 )
	{
		Point3f p1 = new Point3f( x1, y1, z1 );
		Point3f p2 = new Point3f( x2, y2, z2 );
		Point3f p3 = new Point3f( x3, y3, z3 );
		Trianglef t = new Trianglef( p1, p2, p3 );
		oct.addGeom( t );
		geom.add( t );
	}
	
	public void addLight( float x, float y, float z, float i )
	{
		Point3f v = new Point3f( x, y, z );
		transform( v );
		lights.add( new Light( v, i ) );
	}
	
	public void clear()
	{
		oct = new GeomOctTree();
		lights.clear();
		vertexBuffer.clear();
	}
	
	public void fill( int c )
	{
		color = c;
	}

	
	public void fill( float r, float g, float b )
	{
		color = DGraphics.rgb( (int)r, (int)g, (int)b );
	}

	
	public void fill( float r, float g, float b, float a )
	{
		color = DGraphics.rgba( (int)r, (int)g, (int)b, (int)a );
	}

	public void initialize()
	{
		
		
	}
	
	public void size( int x, int y )
	{
		
	}
	
	public void texture( Image img )
	{	
		
	}
	
	public void update()
	{
			
	}
	
	public void ellipse( float x, float y, float width, float height ){}
	public void frameRate( float r ){}
	public void line( float x1, float y1, float x2, float y2 ){}
	public void line( float x1, float y1, float z1, float x2, float y2, float z2 ){}
	public void stroke( int c ){}
	public void stroke( float r, float g, float b ){}
	public void stroke( float r, float g, float b, float a ){}
	public void text( String text, float x, float y ){}
	public void rect( float x, float y, float width, float height ){}
	public void drawImage( Image im, float x, float y ){}
	public void rotate( float angle ) {}
	public void addKeyListener( KeyListener listener ) {}
	public void color( int c ){}
	public void color( float r, float g, float b ){}
	public void color( float r, float g, float b, float a ){}
	public void drawOval( float x, float y, float width, float height ){}
	public void drawRect( float x, float y, float width, float height ){}
	public void fillOval( float x, float y, float width, float height ){}
	public void fillRect( float x, float y, float width, float height ){}
	public void drawImage( Image im, float x, float y, float width, float height ){}
	
	public int getHeight()
	{
		return im.getHeight();
	}

	public int getWidth()
	{
		return im.getWidth();
	}

	public void textureCoords( float u, float v )
	{
		
	}
	
	public void drawImage( Image img, float dx1, float dy1, float dx2, float dy2, float sx1, float sy1, float sx2, float sy2 ){}

	public Vector2f getStringSize( String text )
	{
		return null;
	}

	public void setFont( Font font )
	{
		
	}

	@Override
	public void color( Color color )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawRoundedRect( float x, float y, float width, float height, float arcWidth, float arcHeight )
	{
		//g.drawRoundRect( (int)x, (int)y, (int)width, (int)height, (int)arcWidth, (int)arcHeight );
	}

	@Override
	public void fillRoundedRect( float x, float y, float width, float height, float arcWidth, float arcHeight )
	{
		//g.fillRoundRect( (int)x, (int)y, (int)width, (int)height, (int)arcWidth, (int)arcHeight );
	}

	public class Light
	{
		Point3f loc;
		float b;
		
		public Light( Point3f loc, float b )
		{
			this.loc = loc;
			this.b = b;
		}
	}
	
	public class ContainerBox extends Boxf
	{
		public ArrayList<Geom> geom = new ArrayList<Geom>();
		
		public ContainerBox( float x, float y, float z, float width, float height, float length )
		{
			super( x, y, z, width, height, length );
		}
		
		public void addGeom( Geom g )
		{
			if( !contains( g ) )
				geom.add( g );
		}
		
		public boolean contains( Geom g )
		{
			return geom.contains( g );
		}
	}
	
	public void setScaleLight( boolean scaleLight )
	{
		this.scaleLight = scaleLight;
	}

	@Override
	public void setClip( int x, int y, int width, int height )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearClip()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLineWidth( float width )
	{
		// TODO Auto-generated method stub
		
	}
}
