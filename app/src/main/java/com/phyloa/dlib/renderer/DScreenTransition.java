package com.phyloa.dlib.renderer;

public interface DScreenTransition<E, F>
{
	public void init( E e );
	public void update( E e, float delta );
	public void renderPre( E e, F f );
	public void renderPost( E e, F f );
	
	public boolean isFinished();
}
