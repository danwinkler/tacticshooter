package com.danwink.tacticshooter.renderer;

import org.newdawn.slick.Graphics;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.gameobjects.Bullet;

public class BulletRenderer
{
	public void render( Graphics g, ClientState cs )
	{
		for( int i = 0; i < cs.bullets.size(); i++ )
		{
			Bullet b = cs.bullets.get( i );
			b.render( g );
		}
	}
}