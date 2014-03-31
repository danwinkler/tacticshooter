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

import net.miginfocom.swing.MigLayout;

public class BuildingDialog implements ActionListener
{
	JDialog dialog;
	
	JTextField name;
	JComboBox<BuildingType> type;
	JTextField radius;
	
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
		radius = new JTextField( String.valueOf( b.radius ) );
		
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
