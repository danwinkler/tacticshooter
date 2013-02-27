package tacticshooter.scenegraph;

import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

public class Triangle
{
	Point3f[] points = new Point3f[3];
	Vector3f[] normals = new Vector3f[3];
	Vector2f[] texCoords = new Vector2f[3];
	
	public Triangle()
	{
		for( int i = 0; i < 3; i++ )
		{
			points[i] = new Point3f();
			normals[i] = new Vector3f();
			texCoords[i] = new Vector2f();
		}
	}
}
