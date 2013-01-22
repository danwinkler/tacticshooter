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
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.renderer.DScreen;
import com.phyloa.dlib.renderer.DScreenHandler;

public class LevelEditor extends DScreen<GameContainer, Graphics> implements DUIListener
{
	Level l;
	
	DUI dui;
	DButton wall;
	DButton floor;
	DButton teamACenter;
	DButton teamBCenter;
	DButton point;
	
	DButton save;
	DButton saveAndExit;
	DButton exitWithoutSaving;
	
	DText fileNameText;
	DTextBox name;
	
	float scrollx, scrolly;
	
	Brush brush = Brush.WALL;
	
	Slick2DRenderer renderer = new Slick2DRenderer();

	public void onActivate( GameContainer gc, DScreenHandler<GameContainer, Graphics> dsh )
	{
		//Wait until menu music is loaded, then make sure its already started before you stop it
		while( StaticFiles.getMusic( "menu" ) == null ){}
		StaticFiles.getMusic( "menu" ).play();
		StaticFiles.getMusic( "menu" ).stop();

		if( dui == null )
		{
			dui = new DUI( new Slick2DEventMapper( gc.getInput() ) );
			
			wall = new DButton( "Wall", 0, gc.getHeight()-100, 150, 50 );
			floor = new DButton( "Floor", 150, gc.getHeight()-100, 150, 50 );
			teamACenter = new DButton( "A Center", 300, gc.getHeight()-100, 150, 50 );
			teamBCenter = new DButton( "B Center", 450, gc.getHeight()-100, 150, 50 );
			point = new DButton( "Point", 600, gc.getHeight()-100, 150, 50 );
			
			save = new DButton( "Save", 0, gc.getHeight()-50, 200, 50 );
			saveAndExit = new DButton( "Save & Exit", 200, gc.getHeight()-50, 200, 50 );
			exitWithoutSaving = new DButton( "Exit", 400, gc.getHeight()-50, 200, 50 );
			
			fileNameText = new DText( "Map Name:", gc.getWidth()-350, gc.getHeight()-50 );
			fileNameText.setColor( java.awt.Color.BLACK );
			name = new DTextBox( gc.getWidth()-250, gc.getHeight()-50, 250, 50 );
			
			dui.add( wall );
			dui.add( floor );
			dui.add( teamACenter );
			dui.add( teamBCenter );
			dui.add( point );
			
			dui.add( save );
			dui.add( saveAndExit );
			dui.add( exitWithoutSaving );
			
			dui.add( fileNameText );
			dui.add( name );
			
			dui.addDUIListener( this );
		}
		dui.setEnabled( true );
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
		
		if( input.getMouseY() < gc.getHeight()-100 )
		{
			if( input.isMouseButtonDown( Input.MOUSE_RIGHT_BUTTON ) )
			{
				int x = (int)((input.getMouseX() + scrollx) / l.tileSize) * l.tileSize + l.tileSize/2;
				int y = (int)((input.getMouseY() + scrolly) / l.tileSize) * l.tileSize + l.tileSize/2;
				
				for( int i = 0; i < l.buildings.size(); i++ )
				{
					Building b = l.buildings.get( i );
					int dx = b.x-x;
					int dy = b.y-y;
					if( dx*dx + dy*dy < 10 )
					{
						l.buildings.remove( i );
						i--;
					}
				}
			}
			
			if( input.isMouseButtonDown( Input.MOUSE_LEFT_BUTTON ) )
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
				
			if( input.isMousePressed( Input.MOUSE_LEFT_BUTTON ) )
			{
				int x = (int)((input.getMouseX() + scrollx) / l.tileSize) * l.tileSize + l.tileSize/2;
				int y = (int)((input.getMouseY() + scrolly) / l.tileSize) * l.tileSize + l.tileSize/2;
				
				boolean addBuilding = true;
				
				for( int i = 0; i < l.buildings.size(); i++ )
				{
					Building b = l.buildings.get( i );
					int dx = b.x-x;
					int dy = b.y-y;
					if( dx*dx + dy*dy < 10 )
					{
						addBuilding = false;
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
		if( brush == Brush.POINT || brush == Brush.CENTERTEAMA || brush == Brush.CENTERTEAMB )
		{
			g.drawOval( x*l.tileSize-50 + l.tileSize/2, y*l.tileSize-50 + l.tileSize/2, 100, 100 );
		}
		else
		{
			g.drawRect( x*l.tileSize, y*l.tileSize, l.tileSize, l.tileSize );
		}
		
		g.popTransform();
		
		g.setColor( new Color( 0, 0, 0, 128 ) );
		g.fillRect( 0, gc.getHeight()-100, gc.getWidth(), 100 );
		
		dui.render( renderer.renderTo( g ) );
		
		g.setColor( Color.black );
		g.drawString( x + ", " + y, 100, 25 );
	}

	public void onExit()
	{
		dui.setEnabled( false );
	}

	public void message( Object o )
	{
		if( o instanceof Level )
		{
			l = (Level)o;
		}
		else if( o instanceof String )
		{
			name.setText( (String)o );
		}
	}
	
	enum Brush
	{
		WALL,
		FLOOR,
		CENTERTEAMA,
		CENTERTEAMB,
		POINT;
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
