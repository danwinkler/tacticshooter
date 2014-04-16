package com.danwink.tacticshooter.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class CodePanel extends JPanel implements ActionListener
{
	Editor editor;
	
	RSyntaxTextArea textArea;
	RTextScrollPane sp;
	
	public CodePanel( Editor editor )
	{
		super();
		this.editor = editor;
		
		this.setLayout( new BorderLayout() );
		
		textArea = new RSyntaxTextArea( 20, 60 );
		textArea.setSyntaxEditingStyle( SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT );
		textArea.setCodeFoldingEnabled( true );
		sp = new RTextScrollPane( textArea );
		
		this.add( sp );
	}

	public void actionPerformed( ActionEvent e )
	{
		
	}

	public void updateCode( String code )
	{
		textArea.setText( code != null ? code : "" );
	}
}