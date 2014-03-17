package com.danwink.tacticshooter.screens;

import java.io.File;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;


import com.danwink.tacticshooter.LevelFileHelper;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.slick.Slick2DEventMapper;
import com.danwink.tacticshooter.slick.Slick2DRenderer;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DScrollPane;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class LevelEditorLoadMap extends DScreen<GameContainer, Graphics> implements DUIListener
{
	DUI dui;
	
	DScrollPane scrollPane;
	
	DButton back;
	
	Slick2DRenderer r = new Slick2DRenderer();
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
		
		scrollPane = new DScrollPane( gc.getWidth()/2-200, 50, 410, 500 );
		File[] files = new File( "levels" ).listFiles();
		if( files != null )
		{
			for( int i = 0; i < files.length; i++ )
			{
				scrollPane.add( new DButton( files[i].getName().replace( ".xml", "" ), 0, i*50, 400, 50 ) );
			}
		}
		scrollPane.setInnerPaneHeight( files.length*50 );
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
				dsh.activate( "editorsetup", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
			} 
			else
			{
				try
				{
					dsh.message( "editor", LevelFileHelper.loadLevel( e.getName() ) );
				} catch( DocumentException e1 )
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				dsh.activate( "editor", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
				dsh.message( "editor", e.getName() );
			}
		}
	}
}
