package com.danwink.tacticshooter.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

@SuppressWarnings( "serial" )
public class EditMenu extends JMenu implements ActionListener
{
	Editor editor;
	
	public EditMenu( Editor editor )
	{
		super( "Edit" );
		this.setMnemonic( 'E' );
		
		JMenuItem resizeMenu = new JMenuItem( "Resize", 'R' );
		resizeMenu.setAccelerator( KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_R, java.awt.Event.CTRL_MASK ) );
		resizeMenu.setActionCommand( "resize" );
		resizeMenu.addActionListener( this );
		this.add( resizeMenu );
		
		this.editor = editor;
	}

	public void actionPerformed( ActionEvent e )
	{
		switch( e.getActionCommand() )
		{
		case "resize":
			editor.showResizeDialog();
			break;
		}
	}
}