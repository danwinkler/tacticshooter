package tacticshooter;

import java.awt.Graphics2D;

import tacticshooter.Building.BuildingType;

import com.phyloa.dlib.renderer.Graphics2DRenderer;

public class Building
{
	Team t;
	BuildingType bt;
	int x;
	int y;
	
	public Building()
	{
		
	}
	
	public Building( int x, int y, BuildingType bt, Team t )
	{
		this.t = t;
		this.bt = bt;
		this.x = x;
		this.y = y;
	}

	public void render( Graphics2DRenderer g )
	{
		g.color( 0, 255, 0 );
		g.fillRect( x, y, 30, 30 );
	}
	
	public enum BuildingType
	{
		CENTER;
	}
}
