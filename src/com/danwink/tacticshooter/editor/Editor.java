package com.danwink.tacticshooter.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.danwink.tacticshooter.editor.BrushPanel.Brush;
import com.danwink.tacticshooter.editor.ModePanel.EditMode;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.TileType;

public class Editor
{	
	public static final int DEFAULT_WIDTH = 1024;
	public static final int DEFAULT_HEIGHT = 768;
	
	public JFrame container;
	public JMenuBar menubar;
	
	public BrushPanel brushPanel;
	public BuildingPanel buildingPanel;
	
	public MapPane mapPane;
	public ModePanel modePanel;
	
	public JScrollPane scrollPane;
	
	NewDialog newDialog;
	BuildingDialog buildingDialog;
	
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
		
		modePanel = new ModePanel( this );
		container.add( modePanel, BorderLayout.NORTH );
		
		mapPane = new MapPane( this );
		
		scrollPane = new JScrollPane( mapPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
		container.add( scrollPane, BorderLayout.CENTER );
			
		container.pack();
		container.setVisible( true );
		
		newLevel( 20, 20 );
	}
	
	public void showNewDialog()
	{
		newDialog = new NewDialog( this );
	}
	
	public void newLevel( int width, int height )
	{
		l = new Level( width, height );
		mapPane.updateLevel();
		
		setMode( EditMode.TILE );
	}
	
	public void setMode( EditMode mode )
	{
		if( brushPanel != null )
		{
			container.remove( brushPanel );
			brushPanel = null;
		}
		
		if( buildingPanel != null )
		{
			container.remove( buildingPanel );
			buildingPanel = null;
		}
		
		switch( mode )
		{
		case BUILDING:
			buildingPanel = new BuildingPanel( this );
			container.add( buildingPanel, BorderLayout.WEST );
			break;
		case CODE:
			break;
		case TILE:
			brushPanel = new BrushPanel( this );
			container.add( brushPanel, BorderLayout.WEST );
			break;
		default:
			break;
		}
		
		container.pack();
	}
	
	public static void main( String[] args )
	{
		Editor e = new Editor();
	}

	public Brush getBrush()
	{
		return brushPanel.getBrush();
	}

	public void redrawLevel()
	{
		mapPane.redrawLevel();
	}
	
	public void interact( int x, int y )
	{
		switch( modePanel.mode )
		{
		case BUILDING:
			for( Building b : l.buildings )
			{
				if( b.x == x*l.tileSize + l.tileSize/2 && b.y == y*l.tileSize + l.tileSize/2 )
				{
					buildingDialog = new BuildingDialog( this, b );
					break;
				}
			}
			break;
		case CODE:
			break;
		case TILE:
			break;
		default:
			break;
		
		}
	}

	public void place( int x, int y )
	{
		switch( modePanel.mode )
		{
		case TILE:
			switch( brushPanel.brush )
			{
			case FLOOR:
				l.tiles[x][y] = TileType.FLOOR;
				break;
			case WALL:
				l.tiles[x][y] = TileType.WALL;
				break;
			default:
				break;
			
			}
			break;
		case BUILDING:
				l.buildings.add( new Building( x*l.tileSize + l.tileSize/2, y*l.tileSize + l.tileSize/2, buildingPanel.type, null ) );
			break;
		case CODE:
			break;
		default:
			break;
		}
		
		redrawLevel();
		container.repaint();
	}
}