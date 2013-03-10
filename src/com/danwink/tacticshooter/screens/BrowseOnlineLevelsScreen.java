package com.danwink.tacticshooter.screens;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import tacticshooter.Slick2DEventMapper;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DPanel;
import com.phyloa.dlib.dui.DScrollPane;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class BrowseOnlineLevelsScreen extends DScreen<GameContainer, Graphics> implements DUIListener
{
	DUI dui;
	DButton back;
	
	DButton prev;
	DButton next;
	
	DScrollPane scroll;
	
	DTextBox search;
	
	DButton nameSort;
	DButton ratingSort;
	DButton downloadSort;
	DButton creatorSort;
	
	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
		
		DPanel panel = new DPanel( gc.getWidth()/2 - 400, gc.getHeight()/2 - 300, 800, 600 );
		dui.add( panel );
		
		scroll = new DScrollPane( 0, 50, 500, 800 );
		panel.add( scroll );
	}
	
	public void update( GameContainer gc, int delta )
	{
		
	}

	public void render( GameContainer gc, Graphics g )
	{
		
	}

	public void onExit()
	{
		
	}

	public void message( Object o )
	{
		
	}
	
	public void event( DUIEvent event )
	{
		
	}
}
