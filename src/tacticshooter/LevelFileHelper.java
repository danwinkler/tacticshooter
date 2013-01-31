package tacticshooter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import tacticshooter.Building.BuildingType;
import tacticshooter.Level.Link;
import tacticshooter.Level.TileType;

public class LevelFileHelper
{
	public static Level loadLevel( String name ) throws DocumentException
	{
		SAXReader reader = new SAXReader();
		Document doc = reader.read( new File( "levels\\" + name + ".xml" ) );
		Node level = doc.selectSingleNode( "//level" );
		Level m = new Level( Integer.parseInt( level.valueOf( "@width" ) ), Integer.parseInt( level.valueOf( "@height" ) ) );
		
		Node map = level.selectSingleNode( "map" );
		List<? extends Node> rows = map.selectNodes( "row" );
		for( int y = 0; y < m.height; y++ )
		{
			String[] vals = rows.get( y ).getText().split( "," );
			for( int x = 0; x < m.width; x++ )
			{
				m.tiles[x][y] = TileType.values()[Integer.parseInt( vals[x] )];
			}
		}
		
		
		//Load Buildings
		List<? extends Node> buildings = doc.selectNodes( "//level/buildings/building" );
		for( Node n : buildings )
		{
			Building b = new Building( Integer.parseInt( n.valueOf( "@x" ) ), Integer.parseInt( n.valueOf( "@y" ) ), BuildingType.valueOf( n.valueOf( "@bt" ) ), n.valueOf( "@team" ).equals( "null" ) ? null : new Team( Integer.valueOf( n.valueOf( "@team" ) ) ) );
			String id = n.valueOf( "@id" );
			if( id != null )
			{
				b.id = Integer.parseInt( id );
			}
			m.buildings.add( b );
		}
		
		//Load Links
		List<? extends Node> links = doc.selectNodes( "//level/links/link" );
		for( Node n : links )
		{
			Link l = new Link( Integer.parseInt( n.valueOf( "@source" ) ), Integer.parseInt( n.valueOf( "@targetX" ) ), Integer.parseInt( n.valueOf( "@targetY" ) ) ) ;
			m.links.add( l );
		}
		
		return m;
	}
	
	public static void saveLevel( String name, Level m ) throws IOException
	{
		File file = new File( "levels\\" + name + ".xml" );
		Document doc = DocumentHelper.createDocument();
		Element level = doc.addElement( "level" );
		level.addAttribute( "width", Integer.toString( m.width ) );
		level.addAttribute( "height", Integer.toString( m.height ) );
		
		//ADD map
		Element layer = level.addElement( "map" );
		for( int y = 0; y < m.height; y++ )
		{
			Element row = layer.addElement( "row" );
			StringBuilder rows = new StringBuilder();
			for( int x = 0; x < m.width; x++ )
			{
				rows.append( m.getTile( x, y ).ordinal() + "," );
			}
			row.setText( rows.toString() );
		}
		
		//ADD buildings
		Element buildings = level.addElement( "buildings" );
		for( int i = 0; i < m.buildings.size(); i++ )
		{
			Building b = m.buildings.get( i );
			Element building = buildings.addElement( "building" );
			building.addAttribute( "x", Integer.toString( b.x ) );
			building.addAttribute( "y", Integer.toString( b.y ) );
			building.addAttribute( "bt", b.bt.name() );
			building.addAttribute( "team", b.t == null ? "null" : Integer.toString( b.t.id ) );
			building.addAttribute( "id", Integer.toString( b.id ) );
		}
		
		//ADD links
		Element links = level.addElement( "links" );
		for( int i = 0; i < m.links.size(); i++ )
		{
			Link l = m.links.get( i );
			Element link = links.addElement( "link" );
			link.addAttribute( "source", Integer.toString( l.source ) );
			link.addAttribute( "targetX", Integer.toString( l.targetX ) );
			link.addAttribute( "targetY", Integer.toString( l.targetY ) );
		}
		
		XMLWriter writer = new XMLWriter(
	            new FileWriter( file )
	        );
	    writer.write( doc );
	    writer.close();
	}
}
