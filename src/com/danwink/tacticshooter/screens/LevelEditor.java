package com.danwink.tacticshooter.screens;

import java.io.File;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;

import tacticshooter.Building;
import tacticshooter.Building.BuildingType;
import tacticshooter.Level;
import tacticshooter.Level.Link;
import tacticshooter.Level.TileType;
import tacticshooter.LevelFileHelper;
import tacticshooter.StaticFiles;
import tacticshooter.Team;

public class LevelEditor implements Screen, EventListener
{
	Level l;
	
	Stage stage;
	
	TextButton wall;
	TextButton floor;
	TextButton edge;
	TextButton light;
	TextButton teamACenter;
	TextButton teamBCenter;
	TextButton point;
	
	TextButton pass;
	TextButton gate;
	TextButton addLink;
	TextButton pressurePad;
	TextButton door;
	TextButton grate;
	
	TextButton save;
	TextButton saveAndExit;
	TextButton exitWithoutSaving;
	
	TextButton toggleMirrorBrush;
	
	Label fileNameText;
	TextField name;
	
	float scrollx, scrolly;
	
	Brush brush = Brush.WALL;
	
	MirrorType mirrorType = MirrorType.NONE;
	int nextMirror = 1;
	
	String levelName = "";
	
	Building addLinkSelected;

	public void show()
	{
		//Wait until menu music is loaded, then make sure its already started before you stop it
		while( StaticFiles.getMusic( "menu" ) == null ){}
		StaticFiles.getMusic( "menu" ).play();
		StaticFiles.getMusic( "menu" ).stop();
		
		stage = new Stage();
		Gdx.input.setInputProcessor( stage );
		
		Skin skin = StaticFiles.skin;
		TextButtonStyle tbs = skin.get( TextButtonStyle.class );
		LabelStyle ls = skin.get( LabelStyle.class );
		TextFieldStyle tfs = skin.get( TextFieldStyle.class );
		
		wall = new TextButton( "Wall", tbs );
		floor = new TextButton( "Floor", tbs );
		edge = new TextButton( "Edge" , tbs );
		light = new TextButton( "Light", tbs );
		teamACenter = new TextButton( "A Center", tbs );
		teamBCenter = new TextButton( "B Center", tbs );
		point = new TextButton( "Point", tbs );
		
		pass = new TextButton( "Pass", tbs );
		gate = new TextButton( "Gate", tbs );
		addLink = new TextButton( "Link", tbs );
		pressurePad = new TextButton( "Pressure Pad", tbs );
		
		save = new TextButton( "Save", tbs );
		saveAndExit = new TextButton( "Save & Exit", tbs );
		exitWithoutSaving = new TextButton( "Exit", tbs );
		
		door = new TextButton( "Door", tbs );
		grate = new TextButton( "Grate", tbs );
		
		fileNameText = new Label( "Map Name:", ls );
		name = new TextField( "", tfs );
		name.setText( levelName );
		
		toggleMirrorBrush = new TextButton( "Toggle Mirror Brush", tbs );
	}
	
	public void render( float d )
	{
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
				int tileX = (int)((input.getMouseX() + scrollx) / l.tileSize);
				int tileY = (int)((input.getMouseY() + scrolly) / l.tileSize);
				
				int x = tileX * l.tileSize + l.tileSize/2;
				int y = tileY * l.tileSize + l.tileSize/2;
				
				int x2 = mirrorType == MirrorType.X || mirrorType == MirrorType.XY ? (l.width-1)-tileX : tileX;
				int y2 = mirrorType == MirrorType.Y || mirrorType == MirrorType.XY ? (l.height-1)-tileY : tileY;
				
				for( int i = 0; i < l.buildings.size(); i++ )
				{
					Building b = l.buildings.get( i );
					int dx = b.x-x;
					int dy = b.y-y;
					if( dx*dx + dy*dy < 10 )
					{
						l.buildings.remove( i );
						for( int j = 0; j < l.links.size(); j++ )
						{
							Link link = l.links.get( j );
							if( link.source == b.id )
							{
								l.links.remove( j );
								j--;
							}
						}
						i--;
					}
					
					if( mirrorType != MirrorType.NONE )
					{
						dx = b.x-x2;
						dy = b.y-y2;
						if( dx*dx + dy*dy < 10 )
						{
							l.buildings.remove( i );
							for( int j = 0; j < l.links.size(); j++ )
							{
								Link link = l.links.get( j );
								if( link.source == b.id )
								{
									l.links.remove( j );
									j--;
								}
							}
							i--;
						}
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
				case ADDLINK:
					tile = null;
					break;
				case WALL:
					tile = TileType.WALL;
					break;
				case FLOOR:
					tile = TileType.FLOOR;
					break;
				case LIGHT:
					tile = TileType.LIGHT;
					break;
				case PASS:
					tile = TileType.PASSOPEN;
					break;
				case GATE:
					tile = TileType.GATECLOSED;
					break;
				case DOOR:
					tile = TileType.DOOR;
					break;
				case GRATE:
					tile = TileType.GRATE;
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
					
					if( (up.connectsTo( TileType.WALL ) && down.connectsTo( TileType.WALL ) && left.connectsTo( TileType.WALL ) && right.connectsTo( TileType.WALL )) )
					{
						tile = TileType.WALL;
					}
					else if( up.connectsTo( TileType.WALL ) && !down.connectsTo( TileType.WALL ) && !left.connectsTo( TileType.WALL ) && right.connectsTo( TileType.WALL ) )
					{
						tile = TileType.TRIANGLENE;
					}
					else if( up.connectsTo( TileType.WALL ) && !down.connectsTo( TileType.WALL ) && left.connectsTo( TileType.WALL ) && !right.connectsTo( TileType.WALL ) )
					{
						tile = TileType.TRIANGLENW;
					}
					else if( !up.connectsTo( TileType.WALL ) && down.connectsTo( TileType.WALL ) && !left.connectsTo( TileType.WALL ) && right.connectsTo( TileType.WALL ) )
					{
						tile = TileType.TRIANGLESE;
					}
					else if( !up.connectsTo( TileType.WALL ) && down.connectsTo( TileType.WALL ) && left.connectsTo( TileType.WALL ) && !right.connectsTo( TileType.WALL ) )
					{
						tile = TileType.TRIANGLESW;
					}
					else
					{
						tile = TileType.FLOOR;
					}
					break;
				}
				if( tile != null )
				{
					l.setTile( x, y, tile );
					for( int yy = Math.max( y-1, 0 ); yy <= Math.min( y+1, l.height-1 ); yy++ )
					{
						for( int xx = Math.max( x-1, 0 ); xx <= Math.min( x+1, l.width-1 ); xx++ )
						{
							l.drawAutoTile( levelG, xx, yy, TileType.FLOOR, l.floor );
							l.drawTile( xx, yy, levelG );
						}
					}
					if( mirrorType != MirrorType.NONE )
					{
						l.setTile( x2, y2, tile );
						for( int yy = Math.max( y2-1, 0 ); yy <= Math.min( y2+1, l.height-1 ); yy++ )
						{
							for( int xx = Math.max( x2-1, 0 ); xx <= Math.min( x2+1, l.width-1 ); xx++ )
							{
								l.drawAutoTile( levelG, xx, yy, TileType.FLOOR, l.floor );
								l.drawTile( xx, yy, levelG );
							}
						}
					}
					levelG.flush();
				}
			}
				
			if( input.isMousePressed( Input.MOUSE_LEFT_BUTTON ) )
			{
				int tileX = (int)((input.getMouseX() + scrollx) / l.tileSize);
				int tileY = (int)((input.getMouseY() + scrolly) / l.tileSize);
				
				int x = tileX * l.tileSize + l.tileSize/2;
				int y = tileY * l.tileSize + l.tileSize/2;
				
				int x2 = mirrorType == MirrorType.X || mirrorType == MirrorType.XY ? (l.width-1)-tileX : tileX;
				int y2 = mirrorType == MirrorType.Y || mirrorType == MirrorType.XY ? (l.height-1)-tileY : tileY;
				x2 = x2 * l.tileSize + l.tileSize/2;
				y2 = y2 * l.tileSize + l.tileSize/2;
				
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
						if( mirrorType != MirrorType.NONE && !(x == x2 && y == y2) )
						{
							pointBuilding = new Building( x2, y2, BuildingType.POINT, null );
							pointBuilding.hold = 0;
							l.buildings.add( pointBuilding );
						}
						break;
					case PRESSUREPAD:
						Building ppBuilding = new Building( x, y, BuildingType.PRESSUREPAD, null );
						ppBuilding.hold = 0;
						l.buildings.add( ppBuilding );
						if( mirrorType != MirrorType.NONE && !(x == x2 && y == y2) )
						{
							ppBuilding = new Building( x2, y2, BuildingType.PRESSUREPAD, null );
							ppBuilding.hold = 0;
							l.buildings.add( ppBuilding );
						}
						break;
					}
				}
			}
		}
		
		if( input.isKeyDown( Input.KEY_R ) )
		{
			l.renderFloor( levelG );
			l.render( levelG );
			levelG.flush();
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
		g.drawImage( levelTexture, 0, 0 );
		l.renderBuildings( g );
		l.renderLinks( g );
		
		int x = l.getTileX( gc.getInput().getMouseX() + scrollx );
		int y = l.getTileY( gc.getInput().getMouseY() + scrolly );
		if( brush == Brush.POINT || brush == Brush.CENTERTEAMA || brush == Brush.CENTERTEAMB )
		{
			g.drawOval( x*l.tileSize-50 + l.tileSize/2, y*l.tileSize-50 + l.tileSize/2, 100, 100 );
		}
		else if( brush == Brush.PRESSUREPAD )
		{
			g.drawOval( x*l.tileSize-20 + l.tileSize/2, y*l.tileSize-20 + l.tileSize/2, 40, 40 );
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
		g.drawString( "Brush: " + brush.name(), 500, 25 );
	}

	public void onExit()
	{
		dui.setEnabled( false );
		gc.getInput().removeListener( this );
		scrollx = 0;
		scrolly = 0;
		brush = Brush.FLOOR;
	}

	public void message( Object o )
	{
		if( o instanceof Level )
		{
			l = (Level)o;
		}
		else if( o instanceof String )
		{
			levelName = (String)o;
			if( name != null )
			{
				name.setText( levelName );
			}
		}
	}
	
	enum Brush
	{
		WALL,
		FLOOR,
		TRIANGLE,
		LIGHT,
		CENTERTEAMA,
		CENTERTEAMB,
		POINT,
		PASS,
		GATE,
		ADDLINK, 
		PRESSUREPAD,
		DOOR,
		GRATE;
	}
	
	public void event( DUIEvent event )
	{
		DUIElement e = event.getElement();
		if( e instanceof TextButton && event.getType() == TextButton.MOUSE_UP )
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
			else if( e == light )
			{
				brush = Brush.LIGHT;
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
			else if( e == pass )
			{
				brush = Brush.PASS;
			}
			else if( e == gate )
			{
				brush = Brush.GATE;
			}
			else if( e == addLink )
			{
				brush = Brush.ADDLINK;
			}
			else if( e == pressurePad )
			{
				brush = Brush.PRESSUREPAD;
			}
			else if( e == door )
			{
				brush = Brush.DOOR;
			}
			else if( e == grate )
			{
				brush = Brush.GRATE;
			}
			else if( e == toggleMirrorBrush )
			{
				mirrorType = MirrorType.values()[nextMirror];
				nextMirror = (nextMirror+1) % MirrorType.values().length;
			}
			else if( e == save )
			{
				try
				{
					LevelFileHelper.saveLevel( name.getText().replace( ".", "" ), l );
				} catch( IOException e1 )
				{
					e1.printStackTrace();
				}
			}
			else if( e == saveAndExit )
			{
				try
				{
					LevelFileHelper.saveLevel( name.getText().replace( ".", "" ), l );
				} catch( IOException e1 )
				{
					e1.printStackTrace();
				}
				dsh.activate( "home", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
			}
			else if( e == exitWithoutSaving )
			{
				dsh.activate( "home", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn() );
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

	@Override
	public void mouseClicked( int arg0, int arg1, int arg2, int arg3 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged( int arg0, int arg1, int arg2, int arg3 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved( int arg0, int arg1, int arg2, int arg3 )
	{
		// TODO Auto-generated method stub
		
	}

	public void mousePressed( int button, int mx, int my )
	{
		int tileX = l.getTileX( (mx + scrollx) );
		int tileY = l.getTileY( (my + scrolly) );
		
		int x = tileX * l.tileSize + l.tileSize/2;
		int y = tileY * l.tileSize + l.tileSize/2;
		
		if( brush == Brush.ADDLINK )
		{
			if( addLinkSelected == null )
			{
				for( int i = 0; i < l.buildings.size(); i++ )
				{
					Building b = l.buildings.get( i );
					int dx = b.x-x;
					int dy = b.y-y;
					if( dx*dx + dy*dy < 10 )
					{
						addLinkSelected = b;
						break;
					}
				}
			}
			else
			{
				l.links.add( new Link( addLinkSelected.id, tileX, tileY ) );
				addLinkSelected = null;
			}
		}
	}

	@Override
	public void mouseReleased( int arg0, int arg1, int arg2 )
	{
		
	}

	@Override
	public void mouseWheelMoved( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputEnded()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputStarted()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isAcceptingInput()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setInput( Input arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed( int arg0, char arg1 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased( int arg0, char arg1 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerButtonPressed( int arg0, int arg1 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerButtonReleased( int arg0, int arg1 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerDownPressed( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerDownReleased( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerLeftPressed( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerLeftReleased( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerRightPressed( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerRightReleased( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerUpPressed( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void controllerUpReleased( int arg0 )
	{
		// TODO Auto-generated method stub
		
	}
}
