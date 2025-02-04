package com.phyloa.dlib.dui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.function.Consumer;

import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.phyloa.dlib.renderer.Renderer2D;

import jp.objectclub.vecmath.Vector2f;

public class DButton extends DUIElement {
	public static final int RELEASED = 0;
	public static final int HOVER = 1;
	public static final int PRESSED = 2;
	public static final int MOUSE_DOWN = 0;
	public static final int MOUSE_UP = 1;
	protected int state = 0; // 0 = released, 1 = hover, 2 = pressed

	String text;

	DALTexture background = null;

	Color borderColor = new Color(32, 32, 128);
	Color releaseColor = new Color(128, 128, 255);
	Color hoverColor = new Color(180, 180, 255);
	Color pressedColor = new Color(64, 64, 255);
	Color textColor = new Color(0, 0, 0);

	float imageAlpha = .25f;

	ArrayList<Consumer<DMouseEvent>> mouseUpListeners = new ArrayList<Consumer<DMouseEvent>>();

	public DButton(String text, int x, int y, int width, int height) {
		super(x, y, width, height);
		this.text = text;
		this.name = text;
	}

	public DButton(String text) {
		this.text = text;
		this.name = text;
	}

	public void setBackground(DALTexture background) {
		this.background = background;
	}

	public void render(Renderer2D<DALTexture> r) {
		r.pushMatrix();

		r.translate(x, y);
		int red = 0, blue = 0, green = 0, alpha = 0;
		switch (state) {
			case RELEASED:
				red = releaseColor.getRed();
				green = releaseColor.getGreen();
				blue = releaseColor.getBlue();
				alpha = releaseColor.getAlpha();
				break;
			case HOVER:
				red = hoverColor.getRed();
				green = hoverColor.getGreen();
				blue = hoverColor.getBlue();
				alpha = hoverColor.getAlpha();
				break;
			case PRESSED:
				red = pressedColor.getRed();
				green = pressedColor.getGreen();
				blue = pressedColor.getBlue();
				alpha = pressedColor.getAlpha();
				break;
		}
		r.color(red, green, blue, alpha);
		r.fillRect(0, 0, width - 1, height - 1);

		if (background != null) {
			r.drawImage(background, 0, 0, width, height, new DALColor(1, 1, 1, imageAlpha));
		}

		r.color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), borderColor.getAlpha());
		r.drawRect(0, 0, width - 1, height - 1);

		String[] lines = text.split("\n");
		Vector2f fontHeight = r.getStringSize(text);
		float lineHeight = fontHeight.y * 1.2f;
		float totalHeight = lineHeight * lines.length;
		float top = ((height / 2) - (totalHeight / 2)) + (lineHeight * .25f);
		for (int i = 0; i < lines.length; i++) {
			Vector2f fontSize = r.getStringSize(lines[i]);
			float strX = (width / 2) - (fontSize.x / 2);
			float strY = top + (lineHeight * i);

			r.color(textColor);
			r.text(lines[i], strX, strY);
		}
		r.popMatrix();
	}

	public void update(DUI ui) {

	}

	public void onMouseUp(Consumer<DMouseEvent> c) {
		mouseUpListeners.add(c);
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	public void setReleaseColor(Color releaseColor) {
		this.releaseColor = releaseColor;
	}

	public void setHoverColor(Color hoverColor) {
		this.hoverColor = hoverColor;
	}

	public void setPressedColor(Color pressedColor) {
		this.pressedColor = pressedColor;
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	public int getState() {
		return state;
	}

	public String getText() {
		return text;
	}

	public Color getBorderColor() {
		return borderColor;
	}

	public Color getReleaseColor() {
		return releaseColor;
	}

	public Color getHoverColor() {
		return hoverColor;
	}

	public Color getPressedColor() {
		return pressedColor;
	}

	public Color getTextColor() {
		return textColor;
	}

	public boolean mousePressed(DMouseEvent e) {
		if (isInside(e.x, e.y)) {
			state = PRESSED;
			ui.event(new DUIEvent(this, MOUSE_DOWN));
			ui.setFocus(this);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleased(DMouseEvent e) {
		if (isInside(e.x, e.y)) {
			state = HOVER;
			ui.event(new DUIEvent(this, MOUSE_UP));
			mouseUpListeners.forEach(c -> c.accept(e));
			ui.setFocus(this);
			return true;
		} else {
			state = RELEASED;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(DMouseEvent e) {
		return true;
	}

	@Override
	public void mouseMoved(DMouseEvent e) {
		if (isInside(e.x, e.y)) {
			state = HOVER;
		} else {
			state = RELEASED;
		}
	}

	@Override
	public void mouseEntered(DMouseEvent e) {
		state = HOVER;
	}

	@Override
	public void mouseExited(DMouseEvent e) {
		state = RELEASED;
	}

	@Override
	public void keyPressed(DKeyEvent dke) {

	}

	@Override
	public void keyReleased(DKeyEvent dke) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheel(DMouseEvent dme) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(DKeyEvent dke) {

	}
}
