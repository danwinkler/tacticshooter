package com.phyloa.dlib.dui;

import java.util.ArrayList;

import org.newdawn.slick.Image;

import com.phyloa.dlib.math.Point2i;

import com.phyloa.dlib.renderer.Renderer2D;

public abstract class DUIElement implements DKeyListener, DMouseListener {
	public int x, y, width, height;
	public int relX, relY;
	public boolean isRel = false;
	public RelativePosition relative;
	public String name;

	boolean visible = true;
	protected DUI ui;

	ArrayList<DUIElement> children = new ArrayList<>();

	DUIElement parent;

	// This isn't very well named, but basically is whether or not the last mouse
	// movement event was inside this element
	// as such we can fire off one last mouse move event for elements when the mouse
	// moves outside of them
	public boolean isInside = false;

	public DUIElement() {
	}

	public DUIElement(RelativePosition relative, int x, int y, int width, int height) {
		this.relative = relative;
		this.relX = x;
		this.relY = y;
		this.width = width;
		this.height = height;
		isRel = true;
	}

	public DUIElement(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public abstract void render(Renderer2D<Image> r);

	public abstract void update(DUI ui);

	/**
	 * Caclulate the layout for all child elements
	 */
	protected void calcLayout(DUI dui) {
		// Default behavior is to do nothing unless a relative position is set, in which
		// case to stuff the element in that position
		// If two elements specify the same position, they'll overlap
		for (DUIElement child : children) {
			if (child.isRel) {
				var pos = child.relative.calcPos(child, this);
				child.x = (int) pos.x;
				child.y = (int) pos.y;
			}
		}
	}

	/**
	 * Called when you want to recalculate the whole layout
	 */
	public void doLayout(DUI dui) {
		for (DUIElement child : children) {
			child.doLayout(dui);
		}
		calcLayout(dui);
	}

	public void setRelativePosition(RelativePosition relative, int x, int y) {
		this.relative = relative;
		this.relX = x;
		this.relY = y;
		isRel = true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public DUIElement setSize(int width, int height) {
		this.width = width;
		this.height = height;
		return this;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isInside(int mx, int my) {
		return mx >= x && my >= y && mx <= x + width && my <= y + height;
	}

	public Point2i getScreenLocation() {
		if (parent != null) {
			Point2i p = parent.getScreenLocation();
			p.x += x;
			p.y += y;
			return p;
		} else {
			return new Point2i(x, y);
		}
	}

	public void setUI(DUI ui) {
		this.ui = ui;
		for (int i = 0; i < children.size(); i++) {
			children.get(i).setUI(ui);
		}
	}

	public void handleChildrenMouseMoved(DMouseEvent e) {
		if (visible) {
			e.x = e.x - this.x;
			e.y = e.y - this.y;
			for (int i = 0; i < children.size(); i++) {
				DUIElement el = children.get(i);
				boolean inside = el.isInside(e.x, e.y);
				if ((inside || el.isInside) && el.isVisible()) {
					el.isInside = inside;
					el.mouseMoved(e);
					el.handleChildrenMouseMoved(e);
				}
			}
			e.x += this.x;
			e.y += this.y;
		}
	}

	public boolean handleChildrenMousePressed(DMouseEvent e) {
		if (visible) {
			boolean thisHandled = false;
			boolean childrenHandled = false;
			ui.setFocus(this);
			e.x = e.x - this.x;
			e.y = e.y - this.y;
			for (int i = 0; i < children.size(); i++) {
				DUIElement el = children.get(i);
				if (el.isInside(e.x, e.y) && el.isVisible()) {
					thisHandled = el.mousePressed(e);
					childrenHandled = el.handleChildrenMousePressed(e);
					if (thisHandled || childrenHandled) {
						break;
					}
				}
			}
			e.x += this.x;
			e.y += this.y;
			return thisHandled || childrenHandled;
		}
		return false;
	}

	public boolean handleChildrenMouseReleased(DMouseEvent e) {
		if (visible) {
			boolean thisHandled = false;
			boolean childrenHandled = false;
			e.x = e.x - this.x;
			e.y = e.y - this.y;
			for (int i = 0; i < children.size(); i++) {
				DUIElement el = children.get(i);
				if (el.isInside(e.x, e.y) && el.isVisible()) {
					thisHandled = el.mouseReleased(e);
					childrenHandled = el.handleChildrenMouseReleased(e);
					if (thisHandled || childrenHandled) {
						break;
					}
				}
			}
			e.x += this.x;
			e.y += this.y;
			return thisHandled || childrenHandled;
		}
		return false;
	}

	public boolean handleChildrenMouseDragged(DMouseEvent e) {
		if (visible) {
			boolean thisHandled = false;
			boolean childrenHandled = false;
			e.x = e.x - this.x;
			e.y = e.y - this.y;
			for (int i = 0; i < children.size(); i++) {
				DUIElement el = children.get(i);
				boolean inside = el.isInside(e.x, e.y);
				if ((inside || el.isInside) && el.isVisible()) {
					el.isInside = inside;
					thisHandled = el.mouseDragged(e);
					childrenHandled = el.handleChildrenMouseDragged(e);
				}
			}
			e.x += this.x;
			e.y += this.y;
			return thisHandled || childrenHandled;
		}
		return false;
	}

	public void add(DUIElement e) {
		children.add(e);
		e.parent = this;
		e.setUI(ui);
	}

	public void remove(DUIElement e) {
		children.remove(e);
	}

	public void updateChildren(DUI dui) {
		if (visible) {
			for (int i = 0; i < children.size(); i++) {
				if (children.get(i).visible) {
					children.get(i).update(dui);
					children.get(i).updateChildren(dui);
				}
			}
		}
	}

	public void renderChildren(Renderer2D r) {
		if (visible) {
			r.pushMatrix();
			r.translate(x, y);
			for (int i = children.size() - 1; i >= 0; i--) {
				if (children.get(i).visible) {
					children.get(i).render(r);
					children.get(i).renderChildren(r);
				}
			}
			r.popMatrix();
		}
	}

	public void handleChildrenMouseWheel(DMouseEvent e) {
		if (visible) {
			e.x = e.x - this.x;
			e.y = e.y - this.y;
			for (int i = 0; i < children.size(); i++) {
				DUIElement el = children.get(i);
				if (el.isInside(e.x, e.y)) {
					el.mouseWheel(e);
					el.handleChildrenMouseWheel(e);
				}
			}
			e.x += this.x;
			e.y += this.y;
		}
	}

	public void losingTopPanel(DUIElement e) {

	}

	public void clearChildren() {
		children.clear();
	}

	public ArrayList<DUIElement> getChildren() {
		return children;
	}
}
