package com.phyloa.dlib.dui;

import java.awt.Color;
import java.awt.Font;

import com.phyloa.dlib.renderer.Renderer2D;

import jp.objectclub.vecmath.Vector2f;

public class DText extends DUIElement {
	String text;
	Font font;
	Color color = new Color(128, 128, 255);
	boolean centered;

	public DText(String text, boolean centered) {
		this(text, 0, 0, centered);
	}

	public DText(String text, int x, int y) {
		super(x, y, 0, 0);
		this.text = text;
	}

	public DText(String string, int x, int y, boolean centered) {
		this(string, x, y);
		this.centered = centered;
	}

	public void render(Renderer2D r) {
		if (font != null) {

		}
		r.color(ui.theme.borderColor);
		Vector2f size = r.getStringSize(text);
		if (centered) {
			r.text(text, x + width / 2 - size.x / 2, y + height / 2 - size.y / 2);
		} else {
			r.text(text, x, y);
		}
	}

	public void update(DUI ui) {

	}

	public String getText() {
		return text;
	}

	public Font getFont() {
		return font;
	}

	public Color getColor() {
		return color;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setCentered(boolean centered) {
		this.centered = centered;
	}

	@Override
	public void keyPressed(DKeyEvent dke) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(DKeyEvent dke) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(DKeyEvent dke) {

	}

	@Override
	public void mouseEntered(DMouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(DMouseEvent e) {
		// TODO Auto-generated method stub

	}

	public boolean mousePressed(DMouseEvent e) {
		return false;
	}

	public boolean mouseReleased(DMouseEvent e) {
		return false;
	}

	@Override
	public void mouseMoved(DMouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean mouseDragged(DMouseEvent e) {
		return false;
	}

	@Override
	public void mouseWheel(DMouseEvent dme) {
		// TODO Auto-generated method stub

	}
}
