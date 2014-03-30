package com.danwink.tacticshooter.editor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.phyloa.dlib.renderer.Graphics2DIRenderer;

public class MapPane extends JPanel implements MouseListener, MouseMotionListener
{
	Graphics2DIRenderer r;
	LevelRenderer lr;
	public BufferedImage image;
	
	Editor editor;

	public MapPane( Editor editor )
	{
		this.editor = editor;
	}
	
	public void updateLevel()
	{
		r = new Graphics2DIRenderer( editor.l.width * editor.l.tileSize, editor.l.height * editor.l.tileSize );
		lr = new LevelRenderer( editor.l );
		lr.render( r );
		
		setSize( editor.l.width * editor.l.tileSize, editor.l.height * editor.l.tileSize );
		setPreferredSize( new Dimension( editor.l.width * editor.l.tileSize, editor.l.height * editor.l.tileSize ) );
		image = (BufferedImage)r.getImage();
		
		this.addMouseListener( this );
		this.addMouseMotionListener( this );
	}
	
	public void redrawLevel()
	{
		lr.render( r );
	}

	public void paintComponent( Graphics g )
	{
		if( image != null ) g.drawImage( image, 0, 0, null );
	}

	public void mouseClicked( MouseEvent e )
	{
		
	}

	public void mouseEntered( MouseEvent e )
	{
		
	}

	public void mouseExited( MouseEvent e )
	{
		
	}

	public void mousePressed( MouseEvent e )
	{
		int x = editor.l.getTileX( e.getX() );
		int y = editor.l.getTileY( e.getY() );
		editor.l.setTile( x, y, editor.getTileBrush() );
		editor.redrawLevel();
		editor.container.repaint();
	}

	public void mouseReleased( MouseEvent e )
	{
		
	}

	public void mouseDragged( MouseEvent e )
	{
		int x = editor.l.getTileX( e.getX() );
		int y = editor.l.getTileY( e.getY() );
		editor.l.setTile( x, y, editor.getTileBrush() );
		editor.redrawLevel();
		editor.container.repaint();
	}

	public void mouseMoved( MouseEvent e )
	{
		
	}
}
