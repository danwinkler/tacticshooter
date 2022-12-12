package com.phyloa.dlib.dui;

import java.awt.Color;

import com.phyloa.dlib.math.Point2i;

import com.phyloa.dlib.renderer.Renderer2D;

public class DScrollPane<ImageClass> extends DUIElement {
	Color borderColor = new Color(32, 32, 128);
	Color barColor = new Color(128, 128, 255);
	int scrollx;
	int scrolly;

	int innerPaneHeight;

	public DScrollPane(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public void setInnerPaneHeight(int height) {
		this.innerPaneHeight = height;
	}

	public void keyPressed(DKeyEvent dke) {

	}

	public void keyReleased(DKeyEvent dke) {

	}

	public void mouseEntered(DMouseEvent e) {

	}

	public void mouseExited(DMouseEvent e) {

	}

	public boolean mousePressed(DMouseEvent e) {
		return false;
	}

	public boolean mouseReleased(DMouseEvent e) {
		return false;
	}

	public void mouseMoved(DMouseEvent e) {

	}

	public void mouseDragged(DMouseEvent e) {

	}

	public void render(Renderer2D r) {
		r.pushMatrix();
		r.translate(x, y);
		r.color(ui.theme.backgroundColor);
		r.fillRect(0, 0, width, height);
		r.color(barColor);
		r.fillRect(width - 10, 0, 10, height);
		r.color(borderColor);
		float barsize = Math.min(Math.max((height / (float) innerPaneHeight) * height, 20), height);
		r.fillRect(width - 10, scrolly / (float) (innerPaneHeight) * height, 10, barsize);

		r.drawRect(0, 0, width, height);
		r.popMatrix();
	}

	public void update(DUI ui) {

	}

	@Override
	public void renderChildren(Renderer2D r) {
		if (visible) {
			Point2i pos = getScreenLocation();
			r.setClip(pos.x, pos.y, width, height);
			r.pushMatrix();
			r.translate(x, y);
			r.translate(-scrollx, -scrolly);
			for (int i = 0; i < children.size(); i++) {
				children.get(i).render(r);
				children.get(i).renderChildren(r);
			}
			r.popMatrix();
			r.clearClip();
		}
	}

	public void handleChildrenMouseMoved(DMouseEvent e) {
		if (visible) {
			int tx = e.x;
			int ty = e.y;
			e.x = e.x - this.x + scrollx;
			e.y = e.y - this.y + scrolly;
			for (int i = 0; i < children.size(); i++) {
				DUIElement el = children.get(i);
				boolean inside = el.isInside(e.x, e.y);
				if (inside || el.isInside) {
					el.isInside = inside;
					el.mouseMoved(e);
					el.handleChildrenMouseMoved(e);
				}
			}
			e.x = tx;
			e.y = ty;
		}
	}

	public boolean handleChildrenMousePressed(DMouseEvent e) {
		if (visible) {
			boolean thisHandled = false;
			boolean childrenHandled = false;
			int tx = e.x;
			int ty = e.y;
			ui.setFocus(this);
			e.x = e.x - this.x + scrollx;
			e.y = e.y - this.y + scrolly;
			for (int i = 0; i < children.size(); i++) {
				DUIElement el = children.get(i);
				if (el.isInside(e.x, e.y)) {
					thisHandled = el.mousePressed(e);
					childrenHandled = el.handleChildrenMousePressed(e);
					if (thisHandled || childrenHandled) {
						break;
					}
				}
			}
			e.x = tx;
			e.y = ty;
			return thisHandled || childrenHandled;
		}
		return false;
	}

	public boolean handleChildrenMouseReleased(DMouseEvent e) {
		if (visible) {
			boolean thisHandled = false;
			boolean childrenHandled = false;
			int tx = e.x;
			int ty = e.y;
			e.x = e.x - this.x + scrollx;
			e.y = e.y - this.y + scrolly;
			for (int i = 0; i < children.size(); i++) {
				DUIElement el = children.get(i);
				if (el.isInside(e.x, e.y)) {
					thisHandled = el.mouseReleased(e);
					childrenHandled = el.handleChildrenMouseReleased(e);
					if (thisHandled || childrenHandled) {
						break;
					}
				}
			}
			e.x = tx;
			e.y = ty;

			return thisHandled || childrenHandled;
		}
		return false;
	}

	@Override
	public void mouseWheel(DMouseEvent dme) {
		scrolly -= dme.wheel * .5f;
		if (scrolly > innerPaneHeight - height)
			scrolly = innerPaneHeight - height;
		if (scrolly < 0)
			scrolly = 0;
	}
}
