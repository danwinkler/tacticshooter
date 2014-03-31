package com.danwink.tacticshooter.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;

public class BrushPanel extends JPanel implements ActionListener
{
	Editor editor;
	
	Brush brush = Brush.FLOOR;
	
	public BrushPanel( Editor editor )
	{
		super();
		this.editor = editor;
		
		this.setLayout( new MigLayout() );
		
		ButtonGroup brushes = new ButtonGroup();
		JToggleButton floor = new JToggleButton( new ImageIcon( editor.mapPane.lr.floor.getSubimage( 32, 64, 32, 32 ) ) );
		JToggleButton wall = new JToggleButton( new ImageIcon( editor.mapPane.lr.wall.getSubimage( 0, 0, 32, 32 ) ) );
		
		floor.setActionCommand( "floor" );
		wall.setActionCommand( "wall" );
		
		floor.addActionListener( this );
		wall.addActionListener( this );
		
		brushes.add( floor );
		brushes.add( wall );
		this.add( floor, "wrap" );
		this.add( wall, "wrap" );
		
		floor.setSelected( true );
	}

	public Brush getBrush()
	{
		return brush;
	}

	public void actionPerformed( ActionEvent e )
	{
		switch( e.getActionCommand() )
		{
		case "floor": brush = Brush.FLOOR; break;
		case "wall": brush = Brush.WALL; break;
		}
	}
	
	public enum Brush
	{
		FLOOR,
		WALL
	}
}
