package com.danwink.tacticshooter.screens;

import java.io.IOException;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import tacticshooter.Building;
import tacticshooter.Building.BuildingType;
import tacticshooter.Level;
import tacticshooter.LevelFileHelper;
import tacticshooter.Slick2DEventMapper;
import tacticshooter.Slick2DRenderer;
import tacticshooter.StaticFiles;
import tacticshooter.Team;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class LevelEditor implements DScreen<GameContainer, Graphics>, DUIListener
{
	DScreenHandler<GameContainer, Graphics> dsh;
	GameContainer gc;
	
	Level l;
	
	DUI dui;
	DButton wall;
	DButton floor;
	DButton teamACenter;
	DButton teamBCenter;
	DButton point;
	DButton deleteBuilding;
	
	DButton save;
	DButton saveAndExit;
	DButton exitWithoutSaving;
	DTextBox name;
	
	float scrollx, scrolly;
	
	Brush brush = Brush.WALL;
	
	Slick2DRenderer renderer = new Slick2DRenderer();

	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		this.dsh = dsh;
		this.gc = gc;
		
		StaticFiles.getMusic( "menu" ).stop();
		
		dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
		
		wall = new DButton( "Wall", 0, gc.getHeight()-50, 100, 50 );
		floor = new DButton( "Floor", 100, gc.getHeight()-50, 100, 50 );
		teamACenter = new DButton( "A Center", 200, gc.getHeight()-50, 100, 50 );
		teamBCenter = new DButton( "B Center", 300, gc.getHeight()-50, 100, 50 );
		point = new DButton( "Point", 400, gc.getHeight()-50, 100, 50 );
		deleteBuilding = new DButton( "Delete Building", 0, gc.getHeight()-100, 100, 50 );
		
		
		save = new DButton( "Save", 500, gc.getHeight()-50, 100, 50 );
		saveAndExit = new DButton( "Save & Exit", 600, gc.getHeight()-50, 100, 50 );
		exitWithoutSaving = new DButton( "Exit", 700, gc.getHeight()-50, 100, 50 );
		name = new DTextBox( 800, gc.getHeight()-50, 200, 50 );
		
		dui.add( wall );
		dui.add( floor );
		dui.add( teamACenter );
		dui.add( teamBCenter );
		dui.add( point );
		dui.add( deleteBuilding );
		
		dui.add( save );
		dui.add( saveAndExit );
		dui.add( exitWithoutSaving );
		dui.add( name );
		
		dui.addDUIListener( this );
	}
	
	public void update( GameContainer gc, int delta )
	{
		Input input = gc.getInput();
		if( input.isKeyDown( Input.KEY_DOWN ) )
		{
			scrolly += 1;
		}
		if( input.isKeyDown( Input.KEY_UP ) )
		{
			scrolly -= 1;
		}
		if( input.isKeyDown( Input.KEY_LEFT ) )
		{
			scrollx -= 1;
		}
		if( input.isKeyDown( Input.KEY_RIGHT ) )
		{
			scrollx += 1;
		}
		
		if( input.isMouseButtonDown( Input.MOUSE_LEFT_BUTTON ) && input.getMouseY() < gc.getHeight()-100 )
		{
			int x = l.getTileX( input.getMouseX() + scrollx );
			int y = l.getTileY( input.getMouseY() + scrolly );
			switch( brush )
			{
			case WALL:
				l.setTile( x, y, 1 );
				break;
			case FLOOR:
				l.setTile( x, y, 0 );
				break;
			}
		}
			
		if( input.isMousePressed( Input.MOUSE_LEFT_BUTTON ) && input.getMouseY() < gc.getHeight()-100 )
		{
			int x = (int)((input.getMouseX() + scrollx) / l.tileSize) * l.tileSize;
			int y = (int)((input.getMouseY() + scrolly) / l.tileSize) * l.tileSize;
			
			boolean addBuilding = true;
			
			for( int i = 0; i < l.buildings.size(); i++ )
			{
				Building b = l.buildings.get( i );
				int dx = b.x-x;
				int dy = b.y-y;
				if( dx*dx + dy*dy < 10 )
				{
					addBuilding = false;
					if( brush == Brush.DELETEBUILDING )
					{
						l.buildings.remove( i );
					}
					break;
				}
			}
			if( addBuilding )
			{
			switch( brush )
			{
				case CENTERTEAMA:
				case CENTERTEAMB:
					Team t = (brush == Brush.CENTERTEAMA) ? Team.a : Team.b;
					for( int i = 0; i < l.buildings.size(); i++ )
					{
						Building b = l.buildings.get( i );
						if( b.bt == BuildingType.CENTER && b.t.id == t.id )
						{
							l.buildings.remove( i );
							i--;
						}
					}
					Building b = new Building( x, y, BuildingType.CENTER, t );
					l.buildings.add( b );
					break;
				case POINT:
					Building pointBuilding = new Building( x, y, BuildingType.POINT, null );
					pointBuilding.hold = 0;
					l.buildings.add( pointBuilding );
					break;
				}
			}
		}
		
		dui.update();
	}

	public void render( GameContainer gc, Graphics g )
	{
		g.pushTransform();
		
		g.setColor( Color.white );
		g.fillRect( 0, 0, gc.getWidth(), gc.getHeight() );
		g.translate( -scrollx, -scrolly );
		g.setColor( Color.gray );
		g.drawRect( 0, 0, l.width*l.tileSize, l.height*l.tileSize );
		l.render( g );
		l.renderBuildings( g );
		
		int x = l.getTileX( gc.getInput().getMouseX() + scrollx );
		int y = l.getTileY( gc.getInput().getMouseY() + scrolly );
		g.drawRect( x*l.tileSize, y*l.tileSize, l.tileSize, l.tileSize );
		
		g.popTransform();
		
		dui.render( renderer.renderTo( g ) );
		
		g.setColor( Color.black );
		g.drawString( x + ", " + y, 100, 25 );
	}

	public void onExit()
	{
		dui.setEnabled( false );
		dui = null;
	}

	public void message( Object o )
	{
		l = (Level)o;
	}
	
	enum Brush
	{
		WALL,
		FLOOR,
		CENTERTEAMA,
		CENTERTEAMB,
		POINT,
		DELETEBUILDING;
	}
	
	public void event( DUIEvent event )
	{
		DUIElement e = event.getElement();
		if( e instanceof DButton && event.getType() == DButton.MOUSE_UP )
		{
			if( e == wall )
			{
				brush = Brush.WALL;
			}
			else if( e == floor )
			{
				brush = Brush.FLOOR;
			}
			else if( e == teamACenter )
			{
				brush = Brush.CENTERTEAMA;
			}
			else if( e == teamBCenter )
			{
				brush = Brush.CENTERTEAMB;
			}
			else if( e == point )
			{
				brush = Brush.POINT;
			}
			else if( e == deleteBuilding )
			{
				brush = Brush.DELETEBUILDING;
			}
			else if( e == save )
			{
				try
				{
					LevelFileHelper.saveLevel( name.getText(), l );
				} catch( IOException e1 )
				{
					e1.printStackTrace();
				}
			}
			else if( e == saveAndExit )
			{
				try
				{
					LevelFileHelper.saveLevel( name.getText(), l );
				} catch( IOException e1 )
				{
					e1.printStackTrace();
				}
				dsh.activate( "home", gc );
			}
			else if( e == exitWithoutSaving )
			{
				dsh.activate( "home", gc );
			}
		}
	}
}
