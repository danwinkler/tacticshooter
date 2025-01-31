package com.phyloa.dlib.game;

import java.util.HashMap;

import com.phyloa.dlib.renderer.DScreenTransition;

public class DScreenHandler<E> {
	HashMap<String, DScreen<E>> screens = new HashMap<String, DScreen<E>>();

	DScreen<E> d = null;

	DScreenTransition<E> out;
	DScreenTransition<E> in;

	DScreen<E> nextScreen;

	public void register(String s, DScreen<E> d) {
		screens.put(s, d);
	}

	public void activate(String s, E e) {
		if (d != null) {
			d.onExit();
		}
		d = screens.get(s);
		synchronized (d) {
			d.dsh = this;
			d.dal = e;
			d.onActivate(e, this);
		}
	}

	public void activate(String s, E e, DScreenTransition<E> out, DScreenTransition<E> in) {
		this.out = out;
		this.in = in;

		nextScreen = get(s);

		if (out != null) {
			out.init(e);
		}
	}

	public DScreen<E> get(String s) {
		return screens.get(s);
	}

	public DScreen<E> get() {
		return d;
	}

	public <F extends DScreen<E>> F get(String s, Class<F> c) {
		return c.cast(screens.get(s));
	}

	public void update(E e, float delta) {
		synchronized (d) {
			if (nextScreen != null) {
				if (out != null) {
					if (out.isFinished()) {
						if (d != null) {
							d.onExit();
						}
						d = nextScreen;
						d.dal = e;
						d.dsh = this;
						d.onActivate(e, this);
						nextScreen = null;
						out = null;
						if (in != null) {
							in.init(e);
						}
					} else {
						out.update(e, delta);
					}
				}
			} else {
				if (in != null) {
					in.update(e, delta);
					if (in.isFinished()) {
						in = null;
					}
				}
				if (d != null) {
					d.update(e, delta);
				}
			}
		}
	}

	public void render(E e) {
		synchronized (d) {
			if (nextScreen != null) {
				if (out != null) {
					out.renderPre(e);
				}
			} else if (in != null) {
				in.renderPre(e);
			}

			if (d != null) {
				d.render(e);
			}

			if (nextScreen != null) {
				if (out != null) {
					out.renderPost(e);
				}
			} else if (in != null) {
				in.renderPost(e);
			}
		}
	}

	public void message(String string, Object o) {
		get(string).message(o);
	}
}
