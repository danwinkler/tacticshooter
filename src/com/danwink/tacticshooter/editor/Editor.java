package com.danwink.tacticshooter.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.miginfocom.swing.MigLayout;

import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.phyloa.dlib.renderer.Graphics2DIRenderer;

public class Editor
{	
	public static final int DEFAULT_WIDTH = 1024;
	public static final int DEFAULT_HEIGHT = 768;
	
	public JFrame container;
	public JMenuBar menubar;
	
	public BrushPanel bp;
	public MapPane mp;
	
	public JScrollPane scrollPane;
	
	NewDialog newDialog;
	
	Level l;
	
	public Editor()
	{
		try
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		catch( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e )
		{
			e.printStackTrace();
		}
		
		container = new JFrame( "TacticShooter Editor" );
		container.setPreferredSize( new Dimension( DEFAULT_WIDTH, DEFAULT_HEIGHT ) );
		container.setSize( DEFAULT_WIDTH, DEFAULT_HEIGHT );
		container.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		menubar = new JMenuBar();
		container.setJMenuBar( menubar );
		
		menubar.add( new FileMenu( this ) );
		
		bp = new BrushPanel( this );
		container.add( bp, BorderLayout.NORTH );
		
		mp = new MapPane( this );
		
		scrollPane = new JScrollPane( mp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
		container.add( scrollPane, BorderLayout.CENTER );
			
		container.pack();
		container.setVisible( true );
	}
	
	public void showNewDialog()
	{
		newDialog = new NewDialog( this );
	}
	
	public void newLevel( int width, int height )
	{
		l = new Level( width, height );
		
		mp.updateLevel();
	}
	
	public static void main( String[] args )
	{
		Editor e = new Editor();
	}

	public TileType getTileBrush()
	{
		return bp.getTileBrush();
	}

	public void redrawLevel()
	{
		mp.redrawLevel();
	}
}
