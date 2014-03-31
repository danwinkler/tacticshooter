package com.danwink.tacticshooter.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;

public class ModePanel extends JPanel implements ActionListener
{
	Editor editor;
	
	EditMode mode = EditMode.TILE;
	
	public ModePanel( Editor editor )
	{
		super();
		this.editor = editor;
		
		this.setLayout( new MigLayout() );
		
		ButtonGroup brushes = new ButtonGroup();
		JToggleButton tile = new JToggleButton( "Tile" );
		JToggleButton building = new JToggleButton( "Building" );
		JToggleButton code = new JToggleButton( "Code" );
		
		tile.setActionCommand( EditMode.TILE.name() );
		building.setActionCommand( EditMode.BUILDING.name() );
		code.setActionCommand( EditMode.CODE.name() );
		
		tile.addActionListener( this );
		building.addActionListener( this );
		code.addActionListener( this );
		
		brushes.add( tile );
		brushes.add( building );
		brushes.add( code );
		this.add( new JLabel( "Mode: " ) );
		this.add( tile );
		this.add( building );
		this.add( code );
		
		tile.setSelected( true );
	}

	public void actionPerformed( ActionEvent e )
	{
		mode = EditMode.valueOf( e.getActionCommand() );
		editor.setMode( mode );
	}
	
	public enum EditMode
	{
		TILE,
		BUILDING,
		CODE
	}
}