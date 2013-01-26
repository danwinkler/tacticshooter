package com.danwink.tacticshooter.screens;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.LevelFileHelper;
import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DCheckBox;
import com.phyloa.dlib.dui.DScrollPane;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;
import com.phyloa.dlib.util.DFile;

public class SelectMapScreen extends DScreen<GameContainer, Graphics> implements DUIListener
{
	DUI dui;
	
	DScrollPane scrollPane;
	
	DButton back;
	
	Slick2DRenderer r = new Slick2DRenderer();
	
	ArrayList<DCheckBox> boxes = new ArrayList<DCheckBox>();
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
		
		String[] selectedFiles = new String[0];
		try
		{
			selectedFiles = DFile.loadText( "mapList.txt" ).split( "\n" );
		} catch( FileNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		scrollPane = new DScrollPane( gc.getWidth()/2-200, 50, 400, 500 );
		File[] files = new File( "levels" ).listFiles();
		if( files != null )
		{
			for( int i = 0; i < files.length; i++ )
			{
				scrollPane.add( new DText( files[i].getName(), 30, i*50 + 12 ) );
				DCheckBox box = new DCheckBox( 10, i*50+30, 20, 20 );
				for( int j = 0; j < selectedFiles.length; j++ )
				{
					if( selectedFiles[j].equals( files[i].getName() ) )
					{
						box.checked = true;
						break;
					}
				}
				box.setName( files[i].getName() );
				boxes.add( box );
				scrollPane.add( box );
			}
		}
		
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
					DCheckBox b = boxes.get( i );
					if( b.checked )
					{
						mapList.append( b.getName() );
						mapList.append( "\n" );
					}
				}
				try
				{
					DFile.saveText( "mapList.txt", mapList.toString() );
				} catch( FileNotFoundException e1 )
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				dsh.activate( "settings", gc );
			} 
		}
	}
}
