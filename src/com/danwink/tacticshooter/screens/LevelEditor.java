package com.danwink.tacticshooter.screens;

import java.io.IOException;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import tacticshooter.Building;
import tacticshooter.Building.BuildingType;
import tacticshooter.Level;
import tacticshooter.Level.TileType;
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
	DButton edge;
	DButton teamACenter;
	DButton teamBCenter;
	DButton point;
	
	DButton save;
	DButton saveAndExit;
	DButton exitWithoutSaving;
	
	DButton toggleMirrorBrush;
	
	DText fileNameText;
	DTextBox name;
	
	float scrollx, scrolly;
	
	Brush brush = Brush.WALL;
	
	Slick2DRenderer renderer = new Slick2DRenderer();
	
	MirrorType mirrorType = MirrorType.NONE;
	int nextMirror = 1;

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
			edge = new DButton( "Edge" , 300, gc.getHeight()-100, 150, 50 );
			teamACenter = new DButton( "A Center", 450, gc.getHeight()-100, 150, 50 );
			teamBCenter = new DButton( "B Center", 600, gc.getHeight()-100, 150, 50 );
			point = new DButton( "Point", 750, gc.getHeight()-100, 150, 50 );
			
			save = new DButton( "Save", 0, gc.getHeight()-50, 200, 50 );
			saveAndExit = new DButton( "Save & Exit", 200, gc.getHeight()-50, 200, 50 );
			exitWithoutSaving = new DButton( "Exit", 400, gc.getHeight()-50, 200, 50 );
			
			fileNameText = new DText( "Map Name:", gc.getWidth()-350, gc.getHeight()-50 );
			fileNameText.setColor( java.awt.Color.BLACK );
			name = new DTextBox( gc.getWidth()-250, gc.getHeight()-50, 250, 50 );
			
			toggleMirrorBrush = new DButton( "Toggle Mirror Brush", gc.getWidth()-200, gc.getHeight()-100, 200, 50 );
			
			dui.add( wall );
			dui.add( floor );
			dui.add( edge );
			dui.add( teamACenter );
			dui.add( teamBCenter );
			dui.add( point );
			
			dui.add( save );
			dui.add( saveAndExit );
			dui.add( exitWithoutSaving );
			
			dui.add( fileNameText );
			dui.add( name );
			
			dui.add( toggleMirrorBrush );
			
			dui.addDUIListener( this );
		}
		dui.setEnabled( true );
	}
	
	public void update( GameContainer gc, int delta )
	{
		float d = delta / 60.f;
		Input input = gc.getInput();
		if( input.isKeyDown( Input.KEY_DOWN ) )
		{
			scrolly += 25 * d;
		}
		if( input.isKeyDown( Input.KEY_UP ) )
		{
			scrolly -= 25 * d;
		}
		if( input.isKeyDown( Input.KEY_LEFT ) )
		{
			scrollx -= 25 * d;
		}
		if( input.isKeyDown( Input.KEY_RIGHT ) )
		{
			scrollx += 25 * d;
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
				TileType tile = TileType.FLOOR;
				int x2 = mirrorType == MirrorType.X || mirrorType == MirrorType.XY ? (l.width-1)-x : x;
				int y2 = mirrorType == MirrorType.Y || mirrorType == MirrorType.XY ? (l.height-1)-y : y;
				switch( brush )
				{
				case WALL:
					tile = TileType.WALL;
					break;
				case FLOOR:
					tile = TileType.FLOOR;
					break;
				case TRIANGLE:
					if( l.getTile( x, y ) == TileType.WALL )
					{
						tile = TileType.WALL; 
						break;
					}
					
					TileType up = l.getTile( x, y-1 );
					TileType down = l.getTile( x, y+1 );
					TileType left = l.getTile( x-1, y );
					TileType right = l.getTile( x+1, y );
					
					if( (up == TileType.WALL && down == TileType.WALL && left == TileType.WALL && right == TileType.WALL) )
					{
						tile = TileType.WALL;
					}
					else if( up == TileType.WALL && down == TileType.FLOOR && left == TileType.FLOOR && right == TileType.WALL )
					{
						tile = TileType.TRIANGLENE;
					}
					else if( up == TileType.WALL && down == TileType.FLOOR && left == TileType.WALL && right == TileType.FLOOR )
					{
						tile = TileType.TRIANGLENW;
					}
					else if( up == TileType.FLOOR && down == TileType.WALL && left == TileType.FLOOR && right == TileType.WALL )
					{
						tile = TileType.TRIANGLESE;
					}
					else if( up == TileType.FLOOR && down == TileType.WALL && left == TileType.WALL && right == TileType.FLOOR )
					{
						tile = TileType.TRIANGLESW;
					}
					else
					{
						tile = TileType.FLOOR;
					}
					break;
				}
				l.setTile( x, y, tile );
				if( mirrorType != MirrorType.NONE )
				{
					l.setTile( x2, y2, tile );
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
		g.drawString( "Mirror Brush: " + mirrorType.name(), 300, 25 );
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
		TRIANGLE,
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
			else if( e == edge )
			{
				brush = Brush.TRIANGLE;
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
			} else if( e == toggleMirrorBrush )
			{
				mirrorType = MirrorType.values()[nextMirror];
				nextMirror = (nextMirror+1) % MirrorType.values().length;
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
	
	public enum MirrorType
	{
		NONE,
		X,
		Y,
		XY;
	}
}
