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

public class FileMenu extends JMenu implements ActionListener
{
	Editor editor;
	
	public FileMenu( Editor editor )
	{
		super( "File" );
		this.setMnemonic( 'F' );
		
		JMenuItem newMenu = new JMenuItem( "New", 'N' );
		newMenu.setAccelerator( KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_N, java.awt.Event.CTRL_MASK ) );
		newMenu.setActionCommand( "new" );
		newMenu.addActionListener( this );
		this.add( newMenu );

		JMenuItem openMenu = new JMenuItem( "Open", 'O' );
		openMenu.setAccelerator( KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_O, java.awt.Event.CTRL_MASK ) );
		openMenu.setActionCommand( "open" );
		openMenu.addActionListener( this );
		this.add( openMenu );

		JMenuItem saveMenu = new JMenuItem( "Save", 'S' );
		saveMenu.setAccelerator( KeyStroke.getKeyStroke( java.awt.event.KeyEvent.VK_S, java.awt.Event.CTRL_MASK ) );
		saveMenu.setActionCommand( "save" );
		saveMenu.addActionListener( this );
		this.add( saveMenu );

		JMenuItem saveAsMenu = new JMenuItem( "Save As...", 'A' );
		saveAsMenu.setActionCommand( "saveas" );
		saveAsMenu.addActionListener( this );
		saveAsMenu.addActionListener( this );
		this.add( saveAsMenu );
		
		this.editor = editor;
	}

	public void actionPerformed( ActionEvent e )
	{
		switch( e.getActionCommand() )
		{
		case "new":
			editor.showNewDialog();
			break;
		case "open":
		{
			JFileChooser ofc = new JFileChooser();
			ofc.setCurrentDirectory( new File( System.getProperty("user.dir") ) );
			int returnVal = ofc.showOpenDialog( editor.container );
			if( returnVal == JFileChooser.APPROVE_OPTION ) {
				try
				{
					editor.loadLevel( LevelFileHelper.loadLevel( ofc.getSelectedFile() ) );
				}
				catch( DocumentException e1 )
				{
					e1.printStackTrace();
				}
			}
			break;
		}
		case "save":
		{
			JFileChooser sfc = new JFileChooser();
			sfc.setCurrentDirectory( new File( System.getProperty("user.dir") ) );
			int returnVal = sfc.showSaveDialog( editor.container );
			if( returnVal == JFileChooser.APPROVE_OPTION ) {
				try
				{
					LevelFileHelper.saveLevel( sfc.getSelectedFile(), editor.l );
				}
				catch( IOException e1 )
				{
					e1.printStackTrace();
				}
			}
			break;
		}
		}
	}
}
