package com.danwink.tacticshooter.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.danwink.tacticshooter.ComputerPlayer.PlayType;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.SlotType;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings( "serial" )
public class OptionsPanel extends JPanel implements ActionListener
{
	Editor editor;
	
	JComboBox<SlotType>[] playerType;
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
			JComboBox<SlotType> pt = new JComboBox<SlotType>( SlotType.values() );
			JComboBox<PlayType> bt = new JComboBox<PlayType>( PlayType.values() );
			
			playerType[i] = pt;
			botType[i] = bt;
			
			pt.setActionCommand( "p-" + i );
			bt.setActionCommand( "b-" + i );
			
			pt.addActionListener( this );
			bt.addActionListener( this );
			
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
		String[] action = e.getActionCommand().split( "-" );
		if( action.length == 2 )
		{
			int i = Integer.parseInt( action[1] );
			if( action[0].equals( "p" ) )
			{
				editor.l.slotOptions[i].st = (SlotType)playerType[i].getSelectedItem();
			}
			else
			{
				editor.l.slotOptions[i].bt = (PlayType)botType[i].getSelectedItem();
			}
		}
	}
	
	public void updateOptions( Level l )
	{
		for( int i = 0; i < 16; i++ )
		{
			playerType[i].setSelectedIndex( l.slotOptions[i].st.ordinal() );
			botType[i].setSelectedIndex( l.slotOptions[i].bt.ordinal() );
		}
	}
}