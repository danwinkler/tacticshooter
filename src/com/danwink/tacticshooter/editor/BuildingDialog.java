package com.danwink.tacticshooter.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Team;

import net.miginfocom.swing.MigLayout;

public class BuildingDialog implements ActionListener
{
	JDialog dialog;
	
	JTextField name;
	JComboBox<BuildingType> type;
	JTextField radius;
	JComboBox team;
	
	JButton confirm;
	JButton cancel;
	
	Editor editor;

	Building b;
	
	public BuildingDialog( Editor editor, Building b )
	{
		this.editor = editor;
		this.b = b;
		
		dialog = new JDialog( editor.container, "New Level" );
		dialog.setResizable( false );
		dialog.getContentPane().setLayout( new MigLayout() );
		
		
		name = new JTextField( b.name );
		type = new JComboBox<BuildingType>( BuildingType.values() );
		type.setSelectedIndex( b.bt.ordinal() );
		radius = new JTextField( String.valueOf( b.radius ) );
		team = new JComboBox( new String[] { "null", "A", "B" } );
		team.setSelectedIndex( b.t == null ? 0 : b.t.id == Team.a.id ? 1 : 2 );
		
		confirm = new JButton( "Confirm" );
		cancel = new JButton( "Cancel" );
		
		confirm.addActionListener( this );
		confirm.setActionCommand( "confirm" );
		cancel.addActionListener( this );
		cancel.setActionCommand( "cancel" );
		
		dialog.add( new JLabel( "Name" ) );
		dialog.add( name, "growx, wrap" );
		
		dialog.add( new JLabel( "Type" ) );
		dialog.add( type, "growx, wrap" );
		
		dialog.add( new JLabel( "Radius" ) );
		dialog.add( radius, "growx, wrap" );
		
		dialog.add( new JLabel( "Team") );
		dialog.add( team, "growx, wrap" );
		
		dialog.add( confirm );
		dialog.add( cancel );
		
		dialog.pack();
		dialog.setLocationRelativeTo( editor.container );
		dialog.setVisible( true );
	}

	@Override
	public void actionPerformed( ActionEvent e )
	{
		switch( e.getActionCommand() )
		{
		case "confirm":
			b.name = name.getText();
			b.bt = (BuildingType)type.getSelectedItem();
			switch( team.getSelectedIndex() )
			{
			case 0: b.t = null; break;
			case 1: b.t = Team.a; break;
			case 2: b.t = Team.b; break;
			}
			b.radius = Float.parseFloat( radius.getText() );
			editor.redrawLevel();
			dialog.setVisible( false );
			break;
		case "cancel":
			dialog.setVisible( false );
			break;
		}
	}
}
