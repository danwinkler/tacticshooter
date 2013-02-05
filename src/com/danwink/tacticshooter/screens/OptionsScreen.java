package com.danwink.tacticshooter.screens;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;
import tacticshooter.StaticFiles;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DCheckBox;
import com.phyloa.dlib.dui.DScrollPane;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;
import com.phyloa.dlib.util.DFile;
import com.phyloa.dlib.util.DOptions;

public class OptionsScreen extends DScreen<GameContainer, Graphics> implements DUIListener
{
	DUI dui;
	
	DScrollPane scrollPane;
	
	DButton back;
	
	Slick2DRenderer r = new Slick2DRenderer();
	
	ArrayList<DTextBox> boxes = new ArrayList<DTextBox>();

	private String optionsFile;

	private String screenToReturn;
	
	public OptionsScreen( String optionsFile, String screenToReturn )
	{
		this.optionsFile = optionsFile;
		this.screenToReturn = screenToReturn;
	}
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
		
		DOptions options = new DOptions( optionsFile );
		
		scrollPane = new DScrollPane( gc.getWidth()/2-200, 50, 410, 500 );
		
		int i = 0;
		for( Entry<String, String> e : options.options.entrySet() )
		{
			scrollPane.add( new DText( e.getKey(), 10, i*50 ) );
			DTextBox box = new DTextBox( 150, i*50, 250, 50 );
			box.setText( e.getValue() );
			box.setName( e.getKey() );
			boxes.add( box );
			scrollPane.add( box );
			i++;
		}
		scrollPane.setInnerPaneHeight( options.options.entrySet().size()*50 );
		
		back = new DButton( "Back", gc.getWidth()/2 - 100, gc.getHeight() - 150, 200, 100 );
		
		dui.add( scrollPane );
		dui.add( back );
		
		dui.addDUIListener( this );
		
		dui.setEnabled( true );
	}
	
	public void update( GameContainer gc, int delta )
	{
		dui.update();
	}

	public void render( GameContainer gc, Graphics g )
	{
		dui.render( r.renderTo( g ) );
	}

	public void onExit()
	{
		dui.setEnabled( false );
		dui = null;
		boxes.clear();
	}

	public void message( Object o )
	{
		
	}

	public void event( DUIEvent event )
	{
		DUIElement e = event.getElement();
		if( e instanceof DButton && event.getType() == DButton.MOUSE_UP )
		{
			if( e == back )
			{
				StringBuilder mapList = new StringBuilder();
				for( int i = 0; i < boxes.size(); i++ )
				{
					DTextBox b = boxes.get( i );
					mapList.append( b.getName() );
					mapList.append( " " );
					mapList.append( b.getText().trim() );
					mapList.append( "\n" );
				}
				try
				{
					DFile.saveText( optionsFile, mapList.toString() );
				} catch( FileNotFoundException e1 )
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				StaticFiles.options = new DOptions( "options.txt" );
				StaticFiles.advOptions = new DOptions( "data" + File.separator + "advoptions.txt" );
				
				gc.setVSync( StaticFiles.options.getB( "vsync" ) );
				dsh.activate( screenToReturn, gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
			} 
		}
	}
}
