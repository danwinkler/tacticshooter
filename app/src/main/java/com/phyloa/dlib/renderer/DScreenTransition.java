package com.phyloa.dlib.renderer;

public interface DScreenTransition<E> {
	public void init(E e);

	public void update(E e, float delta);

	public void renderPre(E e);

	public void renderPost(E e);

	public boolean isFinished();
}
