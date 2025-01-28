package com.phyloa.dlib.game;

public abstract class DScreen<E> {
	public DScreenHandler<E> dsh;
	public E dal;

	public abstract void update(E gc, float delta);

	public abstract void render(E gc);

	public abstract void onActivate(E gc, DScreenHandler<E> dsh);

	public abstract void onExit();

	public abstract void message(Object o);

	public abstract void onResize(int width, int height);
}
