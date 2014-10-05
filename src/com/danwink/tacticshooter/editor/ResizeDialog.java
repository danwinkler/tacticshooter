package com.danwink.tacticshooter.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class ResizeDialog implements ActionListener
{
	JDialog dialog;
	
	JTextField x;
	JTextField y;
	
	JButton confirm;
	JButton cancel;
	
	Editor editor;

	public ResizeDialog( Editor editor )
	{
		this.editor = editor;
		
		dialog = new JDialog( editor.container, "New Level" );
		dialog.setResizable( false );
		dialog.getContentPane().setLayout( new MigLayout() );
		
		x = new JTextField( String.valueOf( editor.l.width ) );
		y = new JTextField( String.valueOf( editor.l.height ) );
		
		confirm = new JButton( "Confirm" );
		cancel = new JButton( "Cancel" );
		
		confirm.addActionListener( this );
		confirm.setActionCommand( "confirm" );
		cancel.addActionListener( this );
		cancel.setActionCommand( "cancel" );
		
		dialog.add( new JLabel( "Width" ) );
		dialog.add( x, "growx, wrap" );
		
		dialog.add( new JLabel( "Height" ) );
		dialog.add( y, "growx, wrap" );
		
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
			try {
				editor.l.resize( Integer.parseInt( x.getText() ), Integer.parseInt( y.getText() ) );
				editor.mapPane.updateLevel();
				editor.redrawLevel();
				dialog.setVisible( false );
			}
			catch( Exception ex )
			{
				ex.printStackTrace();
				JOptionPane.showMessageDialog( 
					editor.container,
					"Width or Height is not a Number",
					"Resize error",
					JOptionPane.ERROR_MESSAGE
				);
			}
			break;
		case "cancel":
			dialog.setVisible( false );
			break;
		}
	}
}