package tacticshooter;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import com.phyloa.dlib.util.DFile;

public class ModelHelpers
{
	public static int loadModel( String filename ) throws FileNotFoundException
	{
		String file = DFile.loadText( filename );
		String[] lines = file.split( "\n" );
		
		ArrayList<Point3f> points = new ArrayList<Point3f>();
		ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
		
		int list = GL11.glGenLists( 1 );
		
		GL11.glNewList( list, GL11.GL_COMPILE );
		
		GL11.glBegin( GL11.GL_TRIANGLES );
		
		for( int i = 0; i < lines.length; i++ )
		{
			String[] line = lines[i].split( " " );
			if( line[0].equals( "v" ) )
			{
				points.add( new Point3f( Float.parseFloat( line[1] ), Float.parseFloat( line[2] ), Float.parseFloat( line[3] ) ) );
			}
			else if( line[0].equals( "vn" ) )
			{
				normals.add( new Vector3f( Float.parseFloat( line[1] ), Float.parseFloat( line[2] ), Float.parseFloat( line[3] ) ) );
			}
			else if( line[0].equals( "f" ) )
			{
				for( int j = 1; j < line.length; j++ )
				{
					String[] parts = line[j].split( "//" );
					Point3f p = points.get( Integer.parseInt( parts[0] )-1 );
					if( parts.length > 1 )
					{
						Vector3f n = normals.get( Integer.parseInt( parts[1] )-1 );
						n.normalize();
						GL11.glNormal3f( n.x, n.y, n.z );
					}
					GL11.glVertex3f( p.x, p.y, p.z );
				}
			}
		}
		
		GL11.glEnd();
		GL11.glEndList();
		
		return list;
	}
}
