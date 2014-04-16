package com.danwink.tacticshooter.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.danwink.tacticshooter.ComputerPlayer.PlayType;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings( "serial" )
public class OptionsPanel extends JPanel implements ActionListener
{
	Editor editor;
	
	JComboBox<String>[] playerType;
	JComboBox<PlayType>[] botType;

	public OptionsPanel( Editor editor )
	{
		super();
		this.editor = editor;
		
		this.setLayout( new MigLayout() );
		
		playerType = new JComboBox[16];
		botType = new JComboBox[16];
		
		this.add( new JLabel( "UMS Player Layout" ), "wrap" );
		
		for( int i = 0; i < 16; i++ )
		{
			JComboBox<String> pt = new JComboBox<String>( new String[] { "HUMAN", "BOT" } );
			JComboBox<PlayType> bt = new JComboBox<PlayType>( PlayType.values() );
			
			playerType[i] = pt;
			botType[i] = bt;
			
			this.add( pt );
			this.add( bt, "wrap" );
			
			if( i == 7 )
			{
				this.add( new JSeparator( SwingConstants.HORIZONTAL ), "span 2, growx, wrap" );
			}
		}
	}

	public void actionPerformed( ActionEvent e )
	{
		
	}
}