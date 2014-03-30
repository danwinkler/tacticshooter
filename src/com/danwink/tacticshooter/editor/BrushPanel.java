package com.danwink.tacticshooter.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.danwink.tacticshooter.gameobjects.Level.TileType;

public class BrushPanel extends JPanel implements ActionListener
{
	Editor editor;
	
	TileType tileBrush = TileType.FLOOR;
	
	public BrushPanel( Editor editor )
	{
		super();
		this.editor = editor;
		
		ButtonGroup brushes = new ButtonGroup();
		JToggleButton floor = new JToggleButton( "Floor" );
		JToggleButton wall = new JToggleButton( "Wall" );
		
		floor.setActionCommand( "floor" );
		wall.setActionCommand( "wall" );
		
		floor.addActionListener( this );
		wall.addActionListener( this );
		
		brushes.add( floor );
		brushes.add( wall );
		this.add( floor );
		this.add( wall );
		
		floor.setSelected( true );
	}

	public TileType getTileBrush()
	{
		return tileBrush;
	}

	public void actionPerformed( ActionEvent e )
	{
		switch( e.getActionCommand() )
		{
		case "floor": tileBrush = TileType.FLOOR; break;
		case "wall": tileBrush = TileType.WALL; break;
		}
	}
}
