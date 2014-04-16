package com.danwink.tacticshooter.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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
	public JTabbedPane tabs;
	
	public JPanel mapEditTab;
	public OptionsPanel optionsPanel;
	public CodePanel codePanel;
	public CodePanel umsPanel;
	
	public BrushPanel brushPanel;
	public BuildingPanel buildingPanel;
	
	public MapPane mapPane;
	public ModePanel modePanel;
	
	public JScrollPane scrollPane;
	
	NewDialog newDialog;
	BuildingDialog buildingDialog;
	
	Level l;
	Building selected;
	
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
		
		tabs = new JTabbedPane();
		
		mapEditTab = new JPanel();
		mapEditTab.setLayout( new BorderLayout() );
		
		modePanel = new ModePanel( this );
		mapEditTab.add( modePanel, BorderLayout.NORTH );
		
		mapPane = new MapPane( this );
		
		scrollPane = new JScrollPane( mapPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
		mapEditTab.add( scrollPane, BorderLayout.CENTER );
			
		optionsPanel = new OptionsPanel( this );
		codePanel = new CodePanel( this );
		umsPanel = new CodePanel( this );
		
		tabs.addTab( "Map", mapEditTab );
		tabs.addTab( "Options", optionsPanel );
		tabs.addTab( "Code", codePanel );
		tabs.addTab( "ums", umsPanel );
		
		container.add( tabs );
		
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
		loadLevel( new Level( width, height ) );
	}
	
	public void loadLevel( Level l )
	{
		this.l = l;
		mapPane.updateLevel();
		codePanel.updateCode( l.code );
		umsPanel.updateCode( l.ums );
		
		setMode( EditMode.TILE );
	}
	
	public void setMode( EditMode mode )
	{
		if( brushPanel != null )
		{
			mapEditTab.remove( brushPanel );
			brushPanel = null;
		}
		
		if( buildingPanel != null )
		{
			mapEditTab.remove( buildingPanel );
			buildingPanel = null;
		}
		
		switch( mode )
		{
		case BUILDING:
			buildingPanel = new BuildingPanel( this );
			mapEditTab.add( buildingPanel, BorderLayout.WEST );
			break;
		case CODE:
			break;
		case TILE:
			brushPanel = new BrushPanel( this );
			mapEditTab.add( brushPanel, BorderLayout.WEST );
			break;
		default:
			break;
		}
		
		mapEditTab.validate();
		//container.pack();
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
	
	public void toggleOptions()
	{
		
	}

	public void toggleCode()
	{
		
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
				setTile( x, y, TileType.FLOOR );
				break;
			case WALL:
				setTile( x, y, TileType.WALL );
				break;
			default:
				break;
			
			}
			break;
		case BUILDING:
				Building b = l.getBuilding( x, y );
				if( b == null )
				{
					l.buildings.add( new Building( x*l.tileSize + l.tileSize/2, y*l.tileSize + l.tileSize/2, buildingPanel.type, null ) );
				}
				else
				{
					selected = b;
				}
			break;
		case CODE:
			break;
		default:
			break;
		}
		
		redrawLevel();
		container.repaint();
	}

	public void setTile( int x, int y, TileType t )
	{
		l.tiles[x][y] = t;
		switch( brushPanel.mirrorMode )
		{
		case "None": break;
		case "X": l.tiles[(l.width-1)-x][y] = t; break;
		case "Y": l.tiles[y][(l.height-1)-y] = t; break;
		case "XY": l.tiles[(l.width-1)-x][(l.height-1)-y] = t; break;
		}
	}

	public void delete()
	{
		if( selected != null )
		{
			l.buildings.remove( selected );
			selected = null;
			redrawLevel();
			container.repaint();
		}
	}

	public void updateLevel()
	{
		l.code = codePanel.textArea.getText();
		l.ums = umsPanel.textArea.getText();
	}
}
