package com.phyloa.dlib.graphics;

import java.util.Stack;

import jp.objectclub.vecmath.AxisAngle4f;
import jp.objectclub.vecmath.Matrix4f;
import jp.objectclub.vecmath.Point3f;
import jp.objectclub.vecmath.Tuple3f;
import jp.objectclub.vecmath.Vector3f;

/**
 * Basically a layer over jp.objectclub.vecmath.Matrix4f to include pushMatrix() and popMatrix().
 * 
 * Designed to be subclassed.
 * 
 * Allows various transformations to be done to the matrix, and then applied to a Point3f or Vector3f.
 * 
 * @author Daniel Winkler
 * 
 */
public class Transformable
{
	Stack<Matrix4f> mats = new Stack<Matrix4f>();
	public Matrix4f mat = new Matrix4f();
	{
		mat.setIdentity();
	}
	
	/**
	 * Translate the matrix in 3D.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void translate( float x, float y, float z )
	{
		Matrix4f opMat = new Matrix4f();
		opMat.set( new Vector3f( x, y, z ) );
		mat.mul( opMat );
	}
	
	/**
	 * Translate the matrix in 2D.
	 * 
	 * @param x
	 * @param y
	 */
	public void translate( float x, float y )
	{
		Matrix4f opMat = new Matrix4f();
		opMat.set( new Vector3f( x, y, 0 ) );
		mat.mul( opMat );
	}
	
	/**
	 * Translate the matrix by a Vector3f.
	 * 
	 * @param t
	 */
	public void translate( Vector3f t )
	{
		Matrix4f opMat = new Matrix4f();
		opMat.set( t );
		mat.mul( opMat );
	}
	
	/**
	 * Rotate the matrix around an axis.
	 * 
	 * @param axis Vector3f to rotate around
	 * @param angle angle in radians
	 */
	public void rotate( Vector3f axis, float angle )
	{
		Matrix4f opMat = new Matrix4f();
		opMat.set( new AxisAngle4f( axis, angle ) );
		mat.mul( opMat );
	}
	
	/**
	 * Rotate the matrix around an axis.
	 * 
	 * @param x x-coord of the vector to rotate around
	 * @param y y-coord of the vector to rotate around
	 * @param z z-coord of the vector to rotate around
	 * @param angle angle in radians
	 */
	public void rotate( float x, float y, float z, float angle )
	{
		Matrix4f opMat = new Matrix4f();
		opMat.set( new AxisAngle4f( x, y, z, angle ) );
		mat.mul( opMat );
	}
	
	/**
	 * Apply a rotation around the x-axis to the matrix.
	 * 
	 * @param angle angle in radians.
	 */
	public void rotateX( float angle )
	{
		Matrix4f opMat = new Matrix4f();
		opMat.rotX( angle );
		mat.mul( opMat );
	}
	
	/**
	 * Apply a rotation around the y-axis to the matrix.
	 * 
	 * @param angle angle in radians.
	 */
	public void rotateY( float angle )
	{
		Matrix4f opMat = new Matrix4f();
		opMat.rotY( angle );
		mat.mul( opMat );
	}
	
	/**
	 * Apply a rotation around the z-axis to the matrix.
	 * 
	 * @param angle angle in radians.
	 */
	public void rotateZ( float angle )
	{
		Matrix4f opMat = new Matrix4f();
		opMat.rotZ( angle );
		mat.mul( opMat );
	}
	
	/**
	 * Apply a scaling operation to the matrix.
	 * @param x scale in x dimension.
	 * @param y scale in y dimension.
	 * @param z scale in z dimension.
	 */
	public void scale( float x, float y, float z )
	{
		Matrix4f opMat = new Matrix4f();
		opMat.set( x );
		mat.mul( opMat );
	}
	
	/**
	 *  Apply a scaling operation in 2D to the matrix.
	 *  
	 * @param x scale in x dimension.
	 * @param y scale in y dimension.
	 */
	public void scale( float x, float y )
	{
		Matrix4f opMat = new Matrix4f();
		opMat.setScale( x );
		mat.mul( opMat );
	}
	
	/**
	 * Scale the matrix
	 * 
	 * @param s scale
	 */
	public void scale( float s )
	{
		Matrix4f opMat = new Matrix4f();
		opMat.setScale( s );
		mat.mul( opMat );
	}
	
	/**
	 * Push the current matrix onto the stack.
	 */
	public void pushMatrix()
	{
		mats.push( new Matrix4f( mat ) );
	}
	
	/**
	 * Pop a matrix off the stack to use as the current transformation matrix.
	 */
	public void popMatrix()
	{
		mat = mats.pop();
	}
	
	/**
	 * Transform a Point3f.
	 * 
	 * @param v reference to the Point3f to be transformed.
	 */
	protected void transform( Point3f v )
	{
		mat.transform( v );
	}
	
	/**
	 * Transform a Vector3f.
	 * 
	 * @param v reference to the Vector3f to be transformed.
	 */
	protected void transform( Vector3f v )
	{
		mat.transform( v );
	}
}
