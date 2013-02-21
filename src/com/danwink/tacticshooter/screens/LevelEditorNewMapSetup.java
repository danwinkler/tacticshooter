package com.danwink.tacticshooter.screens;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.Level;
import tacticshooter.ServerNetworkInterface;
import tacticshooter.GdxEventMapper;
import tacticshooter.GdxRenderer;
import tacticshooter.StaticFiles;
import tacticshooter.TacticServer;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class LevelEditorNewMapSetup extends DScreen<GameContainer, Graphics> implements DUIListener
{
	DUI dui;
	DText widthText;
	DText heightText;
	DTextBox width;
	DTextBox height;
	DButton back;
	DButton okay;
	
	GdxRenderer r;
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		if( dui == null )
		{
			dui = new DUI( new GdxEventMapper( gc.getInput() ) );
			widthText = new DText( "Width:", gc.getWidth()/2 - 150, gc.getHeight()/2-100 );
			widthText.setCentered( true );
			heightText = new DText( "Height:", gc.getWidth()/2 - 150, gc.getHeight()/2 );
			heightText.setCentered( true );
			width = new DTextBox( gc.getWidth()/2 - 100, gc.getHeight()/2-150, 200, 100 );
			height = new DTextBox( gc.getWidth()/2 - 100, gc.getHeight()/2-50, 200, 100 );
			back = new DButton( "Back", gc.getWidth()/2 - 100, gc.getHeight()/2 + 50, 100, 100 );
			okay = new DButton( "Okay", gc.getWidth()/2, gc.getHeight()/2 + 50, 100, 100 );
			
			dui.add( widthText );
			dui.add( heightText );
			dui.add( width );
			dui.add( height );
			dui.add( back );
			dui.add( okay );
			
			dui.addDUIListener( this );
		}
		
		dui.setEnabled( true );
		r = new GdxRenderer();
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
		width.setText( "" );
		height.setText( "" );
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
			else if( e == okay )
			{
				String widthStr = width.getText();
				String heightStr = height.getText();
				if( widthStr.length() > 0 && heightStr.length() > 0 )
				{
					try
					{
						int w = Integer.parseInt( widthStr );
						int h = Integer.parseInt( heightStr );
						dsh.message( "editor", new Level( w, h ) );
						dsh.activate( "editor", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn() );
					} catch( NumberFormatException nfe )
					{
						
					}
				}
			}
		}
	}
}
