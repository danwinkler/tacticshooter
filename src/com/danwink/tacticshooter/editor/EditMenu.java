package com.danwink.tacticshooter.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.dom4j.DocumentException;

import com.danwink.tacticshooter.LevelFileHelper;

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