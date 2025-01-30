package com.phyloa.dlib.dui;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import com.badlogic.gdx.Input.Keys;
import com.phyloa.dlib.renderer.Renderer2D;

import jp.objectclub.vecmath.Vector2f;

public class DTextBox extends DUIElement implements KeyListener {
	Color bgColor = new Color(128, 128, 255);
	Color borderColor = new Color(32, 32, 128);

	String text = "";

	boolean hasFocus = false;

	int cursorBlink = 0;

	int cursorLocation = 0;

	int blinkRate = 30;

	boolean isPassword = false;

	public DTextBox(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public void render(Renderer2D r) {
		r.pushMatrix();
		r.translate(x, y);
		r.color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue());
		r.fillRect(0, 0, width - 1, height - 1);
		r.color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue());
		r.drawRect(0, 0, width - 1, height - 1);
		if (text != null) {
			if (isPassword) {
				for (int i = 0; i < text.length(); i++) {
					r.fillOval(10 + i * 10, height / 2 - 4, 8, 8);
				}
			} else {
				Vector2f fontSize = r.getStringSize(text);
				float strX = (width / 2) - (float) (fontSize.x / 2);
				float strY = (height / 2) - (float) (fontSize.y / 2);
				r.text(text, 3, strY);

				Vector2f cursorVec = r.getStringSize(substring(0, cursorLocation));
				if (ui.focus == this && cursorBlink % blinkRate * 2 < blinkRate) {
					r.line(cursorVec.x + 3, height / 2 - 6, cursorVec.x + 3, height / 2 + 6);
				}
			}
		} else {
			if (ui.focus == this && cursorBlink % blinkRate * 2 < blinkRate) {
				r.line(3, height / 2 - 6, 3, height / 2 + 6);
			}
		}
		r.popMatrix();
	}

	public void update(DUI ui) {
		cursorBlink++;
	}

	public void keyPressed(DKeyEvent e) {
		if (ui.focus == this) {
			int keyCode = e.keyCode;
			if (keyCode == Keys.SHIFT_LEFT || keyCode == Keys.CONTROL_LEFT || keyCode == Keys.ALT_LEFT) {

			} else if (keyCode == Keys.RIGHT) {
				if (cursorLocation < text.length()) {
					cursorLocation++;
				}
			} else if (keyCode == Keys.LEFT) {
				if (cursorLocation > 0) {
					cursorLocation--;
				}
			} else if (e.lctrl && e.keyCode == Keys.V) {
				try {
					text += (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
				} catch (HeadlessException e1) {
					e1.printStackTrace();
				} catch (UnsupportedFlavorException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			ui.event(new DUIEvent(this, keyCode));
		}
	}

	@Override
	public void keyTyped(DKeyEvent e) {
		if (e.keyChar == '\b') {
			if (text != null) {
				if (text.length() > 0) {
					text = substring(0, cursorLocation - 1) + substring(cursorLocation, text.length());
					if (cursorLocation > 0) {
						cursorLocation--;
					}
				}
			}
			return;
		}

		text = substring(0, cursorLocation) + e.keyChar + substring(cursorLocation, text.length());
		cursorLocation++;
	}

	private String substring(int f, int l) {
		if (f >= text.length()) {
			f = text.length();
		}
		if (f < 0) {
			f = 0;
		}
		if (l >= text.length()) {
			l = text.length();
		}
		if (l < 0) {
			l = 0;
		}

		return text.substring(f, l);
	}

	public Color getBgColor() {
		return bgColor;
	}

	public Color getBorderColor() {
		return borderColor;
	}

	public String getText() {
		return text;
	}

	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	public void setText(String text) {
		this.text = text;
		cursorLocation = text.length();
	}

	public void setPasswordInput(boolean isPassword) {
		this.isPassword = isPassword;
	}

	@Override
	public void keyReleased(DKeyEvent dke) {
		// TODO Auto-generated method stub

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
		return true;
	}

	public boolean mouseReleased(DMouseEvent e) {
		return true;
	}

	@Override
	public void mouseMoved(DMouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean mouseDragged(DMouseEvent e) {
		return true;
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheel(DMouseEvent dme) {
		// TODO Auto-generated method stub

	}

}
