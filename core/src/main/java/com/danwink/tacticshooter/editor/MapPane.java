package com.danwink.tacticshooter.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.danwink.tacticshooter.editor.ModePanel.EditMode;
import com.danwink.tacticshooter.gameobjects.Level;
import com.phyloa.dlib.renderer.Graphics2DIRenderer;

@SuppressWarnings("serial")
public class MapPane extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
	Graphics2DIRenderer r;
	LevelRenderer lr;
	public BufferedImage image;

	Editor editor;

	public MapPane(Editor editor) {
		this.editor = editor;

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
	}

	public void updateLevel() {
		r = new Graphics2DIRenderer(editor.l.width * Level.tileSize, editor.l.height * Level.tileSize);
		lr = new LevelRenderer(editor.l);
		redrawLevel();

		setSize(editor.l.width * Level.tileSize, editor.l.height * Level.tileSize);
		setPreferredSize(new Dimension(editor.l.width * Level.tileSize, editor.l.height * Level.tileSize));
		image = (BufferedImage) r.getImage();
	}

	public void redrawLevel() {
		lr.render(r, editor.selected);
		this.repaint();
	}

	public void paintComponent(Graphics g) {
		g.setColor(Color.gray);
		g.fillRect(0, 0, getWidth(), getHeight());
		if (image != null)
			g.drawImage(image, 0, 0, null);
	}

	public void mouseClicked(MouseEvent e) {
		requestFocus();
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
		int x = editor.l.getTileX(e.getX());
		int y = editor.l.getTileY(e.getY());

		if (e.getButton() == MouseEvent.BUTTON1) {
			editor.place(x, y);
			editor.beginUndoableChange();
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			editor.interact(x, y);
		}
	}

	public void mouseReleased(MouseEvent e) {
		editor.endUndoableChange();
	}

	public void mouseDragged(MouseEvent e) {
		if (editor.modePanel.mode == EditMode.TILE) {
			int x = editor.l.getTileX(e.getX());
			int y = editor.l.getTileY(e.getY());
			editor.place(x, y);
		}
	}

	public void mouseMoved(MouseEvent e) {

	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			editor.delete();
		}
	}

	public void keyReleased(KeyEvent e) {

	}

	public void keyTyped(KeyEvent e) {

	}
}
