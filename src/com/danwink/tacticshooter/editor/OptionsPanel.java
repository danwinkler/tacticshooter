package com.danwink.tacticshooter.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class OptionsPanel extends JPanel implements ActionListener
{
	Editor editor;
	
	public OptionsPanel( Editor editor )
	{
		super();
		this.editor = editor;
		
		this.setLayout( new MigLayout() );
		
		
	}

	public void actionPerformed( ActionEvent e )
	{
		
	}
}