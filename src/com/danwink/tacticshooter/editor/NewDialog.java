package com.danwink.tacticshooter.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class NewDialog implements ActionListener
{
	JDialog dialog;
	
	JLabel widthLabel;
	JTextField widthText;
	
	JLabel heightLabel;
	JTextField heightText;
	
	JButton confirm;
	JButton cancel;
	
	Editor editor;
	
	public NewDialog( Editor editor )
	{
		this.editor = editor;
		
		dialog = new JDialog( editor.container, "New Level" );
		dialog.setResizable( false );
		dialog.getContentPane().setLayout( new MigLayout() );
		
		widthLabel = new JLabel( "Width" );
		heightLabel = new JLabel( "Height" );
		
		widthText = new JTextField( "20" );
		heightText = new JTextField( "20" );
		
		widthLabel.setLabelFor( widthText );
		heightLabel.setLabelFor( heightText );
		
		confirm = new JButton( "Confirm" );
		cancel = new JButton( "Cancel" );
		
		confirm.addActionListener( this );
		confirm.setActionCommand( "confirm" );
		cancel.addActionListener( this );
		cancel.setActionCommand( "cancel" );
		
		dialog.add( widthLabel );
		dialog.add( widthText, "growx, wrap" );
		
		dialog.add( heightLabel );
		dialog.add( heightText, "growx, wrap" );
		
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
			editor.newLevel( Integer.parseInt( widthText.getText() ), Integer.parseInt( heightText.getText() ) );
			dialog.setVisible( false );
			break;
		case "cancel":
			dialog.setVisible( false );
			break;
		}
	}
}
