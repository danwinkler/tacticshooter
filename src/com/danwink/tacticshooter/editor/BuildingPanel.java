package com.danwink.tacticshooter.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.danwink.tacticshooter.gameobjects.Building.BuildingType;

import net.miginfocom.swing.MigLayout;

public class BuildingPanel extends JPanel implements ActionListener
{
	Editor editor;
	
	BuildingType type = BuildingType.values()[0];
	
	public BuildingPanel( Editor editor )
	{
		super();
		this.editor = editor;
		
		this.setLayout( new MigLayout() );
		
		ButtonGroup brushes = new ButtonGroup();
		boolean first = true;
		for( BuildingType t : BuildingType.values() )
		{
			JToggleButton building = new JToggleButton( t.name().substring( 0, 1 ) + t.name().toLowerCase().substring( 1 ) );
			building.setActionCommand( t.name() );
			building.addActionListener( this );
			brushes.add( building );
			this.add( building, "wrap" );
			
			if( first )
			{
				building.setSelected( true );
				first = false;
			}
		}
	}

	public void actionPerformed( ActionEvent e )
	{
		type = BuildingType.valueOf( e.getActionCommand() );
	}
}
