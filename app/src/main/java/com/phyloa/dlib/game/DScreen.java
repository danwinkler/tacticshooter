package com.phyloa.dlib.game;

public abstract class DScreen<E, F>
{	
	public DScreenHandler<E, F> dsh;
	public E gc;
	
	public abstract void update( E gc, float delta );
	public abstract void render( E gc, F g );
	public abstract void onActivate( E gc, DScreenHandler<E, F> dsh );
	public abstract void onExit();
	public abstract void message( Object o );
	public abstract void onResize( int width, int height );
}
