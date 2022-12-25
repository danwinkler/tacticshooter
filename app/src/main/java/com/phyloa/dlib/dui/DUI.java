package com.phyloa.dlib.dui;

import java.util.ArrayList;

import org.newdawn.slick.Image;

import com.phyloa.dlib.renderer.Renderer2D;

public class DUI implements DMouseListener, DKeyListener {
	DEventMapper dem;
	ArrayList<DUIListener> listeners = new ArrayList<DUIListener>();
	ArrayList<DDialog> dialogs = new ArrayList<DDialog>();
	ArrayList<DMouseListener> passThroughMouseListeners = new ArrayList<DMouseListener>();
	ArrayList<DKeyListener> passThroughKeyListeners = new ArrayList<DKeyListener>();

	DUIElement focus = null;
	DUIElement hover = null;
	public DPanel rootPane = new DPanel(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);

	// Top panel is for things like dropdown menus, which need to overlay over all
	// other elements breifly, and one at a time
	DUIElement topPanelOwner = rootPane;
	DPanel topPanel = new DPanel(0, 0, 0, 0);

	boolean enabled = true;

	public DUITheme theme = DUITheme.defaultTheme;

	public DUI(DEventMapper dem) {
		this.dem = dem;
		dem.addDKeyListener(this);
		dem.addDMouseListener(this);
		rootPane.ui = this;
		topPanel.ui = this;
		topPanel.visible = false;
	}

	public DUI(DEventMapper dem, int x, int y, int width, int height) {
		this(dem);
		rootPane.setLocation(x, y);
		rootPane.setSize(width, height);
		topPanel.setLocation(x, y);
		topPanel.setSize(width, height);
		topPanel.consumeMouseEvents = true;
	}

	public void setTopPanel(DUIElement owner, DUIElement topPanelChild) {
		topPanelOwner.losingTopPanel(topPanel);
		topPanelOwner = owner;
		topPanel.clearChildren();
		topPanel.add(topPanelChild);
		topPanelChild.setUI(this);
		topPanel.setVisible(true);
		topPanel.doLayout(this);
	}

	public void doLayout() {
		rootPane.doLayout(this);
		topPanel.doLayout(this);
	}

	public void update() {
		if (enabled) {
			if (topPanel.visible) {
				topPanel.update(this);
				topPanel.updateChildren(this);
			}
			rootPane.update(this);
			rootPane.updateChildren(this);

			if (dialogs.size() > 0) {
				DDialog d = dialogs.get(dialogs.size() - 1);
				d.update(this);
				if (d.isComplete()) {
					dialogs.remove(d);
					listeners.remove(d);
					event(new DUIEvent(d));
				}
			}
		}
	}

	public void render(Renderer2D<Image> r) {
		if (enabled) {
			rootPane.render(r);
			rootPane.renderChildren(r);

			for (int i = 0; i < dialogs.size(); i++) {
				dialogs.get(i).render(r);
				dialogs.get(i).renderChildren(r);
			}

			if (topPanel.visible) {
				topPanel.render(r);
				topPanel.renderChildren(r);
			}
		}
	}

	public void addDUIListener(DUIListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	public void addPassthroughMouseListener(DMouseListener l) {
		if (!passThroughMouseListeners.contains(l)) {
			passThroughMouseListeners.add(l);
		}
	}

	public void addPassthroughKeyListener(DKeyListener l) {
		if (!passThroughKeyListeners.contains(l)) {
			passThroughKeyListeners.add(l);
		}
	}

	public void add(DUIElement e) {
		if (!rootPane.children.contains(e)) {
			rootPane.add(e);
			e.setUI(this);
		}
	}

	public void showDialog(DDialog d, int x, int y) {
		d.setLocation(x, y);
		dialogs.add(d);
		d.setUI(this);
	}

	public void remove(DUIElement e) {
		rootPane.remove(e);
	}

	public void event(DUIEvent e) {
		for (int i = 0; i < listeners.size(); i++) {
			DUIListener l = listeners.get(i);
			l.event(e);
		}
	}

	public DUIElement getFocus() {
		return focus;
	}

	public void setFocus(DUIElement focus) {
		this.focus = focus;
	}

	public void mouseMoved(DMouseEvent e) {
		if (enabled) {
			if (topPanel.visible && topPanel.isInside(e.x, e.y)) {
				topPanel.mouseMoved(e);
				topPanel.handleChildrenMouseMoved(e);
			} else {
				rootPane.mouseMoved(e);
				rootPane.handleChildrenMouseMoved(e);
			}

			if (dialogs.size() > 0) {
				DDialog d = dialogs.get(dialogs.size() - 1);
				d.handleChildrenMouseMoved(e);
			}
		}
		passThroughMouseListeners.forEach(l -> l.mouseMoved(e));
	}

	public boolean dialogPresent() {
		return dialogs.size() > 0;
	}

	public boolean mousePressed(DMouseEvent e) {
		boolean handled = false;
		if (enabled) {
			if (topPanel.visible && topPanel.isInside(e.x, e.y)) {
				handled |= topPanel.mousePressed(e);
				handled |= topPanel.handleChildrenMousePressed(e);
			} else {
				handled |= rootPane.mousePressed(e);
				handled |= rootPane.handleChildrenMousePressed(e);
			}

			if (dialogs.size() > 0) {
				DDialog d = dialogs.get(dialogs.size() - 1);
				handled |= d.handleChildrenMousePressed(e);
			}
		}

		if (!handled) {
			passThroughMouseListeners.forEach(l -> l.mousePressed(e));
		}

		return handled;
	}

	public boolean mouseReleased(DMouseEvent e) {
		boolean handled = false;
		if (enabled) {
			if (topPanel.visible && topPanel.isInside(e.x, e.y)) {
				var topPanelHandled = topPanel.mouseReleased(e);
				var topPanelChildrenHandled = topPanel.handleChildrenMouseReleased(e);
				if (topPanelHandled && !topPanelChildrenHandled) {
					topPanel.visible = false;
				} else {
					handled |= topPanelChildrenHandled;
				}
			} else {
				handled |= rootPane.mouseReleased(e);
				handled |= rootPane.handleChildrenMouseReleased(e);
			}

			if (dialogs.size() > 0) {
				DDialog d = dialogs.get(dialogs.size() - 1);
				handled |= d.handleChildrenMouseReleased(e);
			}
		}

		if (!handled) {
			passThroughMouseListeners.forEach(l -> l.mouseReleased(e));
		}

		return handled;
	}

	@Override
	public void keyPressed(DKeyEvent dke) {
		if (enabled && focus != null) {
			focus.keyPressed(dke);
		}

		passThroughKeyListeners.forEach(l -> l.keyPressed(dke));
	}

	@Override
	public void keyReleased(DKeyEvent dke) {
		if (enabled && focus != null) {
			focus.keyReleased(dke);
		}

		passThroughKeyListeners.forEach(l -> l.keyReleased(dke));
	}

	@Override
	public void mouseEntered(DMouseEvent e) {
		passThroughMouseListeners.forEach(l -> l.mouseEntered(e));
	}

	@Override
	public void mouseExited(DMouseEvent e) {
		passThroughMouseListeners.forEach(l -> l.mouseExited(e));
	}

	@Override
	public boolean mouseDragged(DMouseEvent e) {
		boolean handled = false;
		if (enabled) {
			if (topPanel.visible && topPanel.isInside(e.x, e.y)) {
				handled |= topPanel.mouseDragged(e);
				handled |= topPanel.handleChildrenMouseDragged(e);
			} else {
				handled |= rootPane.mouseDragged(e);
				handled |= rootPane.handleChildrenMouseDragged(e);
			}

			if (dialogs.size() > 0) {
				DDialog d = dialogs.get(dialogs.size() - 1);
				handled |= d.handleChildrenMouseDragged(e);
			}
		}

		if (!handled) {
			passThroughMouseListeners.forEach(l -> l.mouseDragged(e));
		}

		return handled;
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled)
			return;
		this.enabled = enabled;
		dem.setEnabled(enabled);
		if (enabled) {
			dem.addDKeyListener(this);
			dem.addDMouseListener(this);
		} else {
			dem.removeDKeyListener(this);
			dem.removeDMouseListener(this);
		}
	}

	@Override
	public void mouseWheel(DMouseEvent dme) {
		if (enabled) {
			if (topPanel.isVisible()) {
				topPanel.mouseWheel(dme);
				topPanel.handleChildrenMouseWheel(dme);
			} else {
				rootPane.mouseWheel(dme);
				rootPane.handleChildrenMouseWheel(dme);
			}
		}

		if (dialogs.size() > 0) {
			DDialog d = dialogs.get(dialogs.size() - 1);
			d.handleChildrenMouseWheel(dme);
		}

		passThroughMouseListeners.forEach(l -> l.mouseWheel(dme));
	}

	public void setTheme(DUITheme theme) {
		this.theme = theme;
	}

	public void resize(int width, int height) {
		rootPane.setSize(width, height);
		topPanel.setSize(width, height);
	}

	public void hideTopPanel() {
		topPanel.visible = false;
	}
}
