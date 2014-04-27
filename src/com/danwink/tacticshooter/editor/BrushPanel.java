package com.danwink.tacticshooter.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

public class BrushPanel extends JPanel implements ActionListener
{
	Editor editor;
	
	Brush brush = Brush.FLOOR;
	String mirrorMode = "None";
	
	public BrushPanel( Editor editor )
	{
		super();
		this.editor = editor;
		
		this.setLayout( new MigLayout() );
		
		ButtonGroup brushes = new ButtonGroup();
		JToggleButton floor = new JToggleButton( new ImageIcon( editor.mapPane.lr.floor.getSubimage( 32, 64, 32, 32 ) ) );
		JToggleButton wall = new JToggleButton( new ImageIcon( editor.mapPane.lr.wall.getSubimage( 0, 0, 32, 32 ) ) );
		JToggleButton door = new JToggleButton( "DOOR" );
		JToggleButton grate = new JToggleButton( "GRATE" );
		
		floor.setActionCommand( "floor" );
		wall.setActionCommand( "wall" );
		door.setActionCommand( "door" );
		grate.setActionCommand( "grate" );
		
		floor.addActionListener( this );
		wall.addActionListener( this );
		door.addActionListener( this );
		grate.addActionListener( this );
		
		
		brushes.add( floor );
		brushes.add( wall );
		brushes.add( door );
		brushes.add( grate );
		
		this.add( floor, "wrap" );
		this.add( wall, "wrap" );
		this.add( door, "wrap" );
		this.add( grate, "wrap" );
		
		floor.setSelected( true );
		
		this.add( new JSeparator( SwingConstants.HORIZONTAL ), "growx, wrap" );
		
		this.add( new JLabel( "Mirror Mode", JLabel.CENTER ), "growx, wrap" );
		
		groupHelper( "None", "X", "Y", "XY", "4" );
	}
	
	private void groupHelper( String... a )
	{
		ButtonGroup b = new ButtonGroup();
		boolean first = true;
		for( String s : a )
		{
			JToggleButton x = new JToggleButton( s );
			x.setActionCommand( s );
			x.addActionListener( this );
			b.add( x );
			this.add( x, "growx, wrap" );
			
			if( first )
			{
				first = false;
				x.setSelected( true );
			}
		}
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
		case "door": brush = Brush.DOOR; break;
		case "grate": brush = Brush.GRATE; break;
		default:
			mirrorMode = e.getActionCommand();
			break;
		}
	}
	
	public enum Brush
	{
		FLOOR,
		WALL,
		GRATE,
		DOOR
	}
}
