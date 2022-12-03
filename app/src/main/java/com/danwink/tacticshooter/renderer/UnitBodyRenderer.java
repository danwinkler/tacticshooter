package com.danwink.tacticshooter.renderer;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SpriteSheet;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.phyloa.dlib.util.DMath;

public class UnitBodyRenderer {
	public Color playerColor = new Color(128, 128, 255);

	public void render(Graphics g, ClientState cs) {
		for (int i = 0; i < cs.units.size(); i++) {
			Unit u = cs.units.get(i);
			drawUnit(g, u, cs);
		}
	}

	public void drawUnit(Graphics g, Unit u, ClientState cs) {
		g.pushTransform();
		g.translate((int) u.x, (int) u.y);
		drawBody(g, u, u.owner.id == cs.player.id, cs);
		g.popTransform();
	}

	public void drawDeadUnit(Graphics g, Unit u, ClientState cs) {
		g.pushTransform();
		g.translate(u.x, u.y);
		g.rotate(0, 0, u.heading / DMath.PI2F * 360);
		drawBody(g, u, false, cs);
		g.popTransform();
	}

	public void drawSpriteBody(Graphics g, Unit u, SpriteSheet baseSheet, SpriteSheet colorSheet, Color color,
			int size) {
		g.pushTransform();
		g.rotate(0, 0, (u.heading / DMath.PI2F * 360) + 90);
		g.drawImage(baseSheet.getSprite(u.frame % 4, 0), -size, -size);
		g.drawImage(colorSheet.getSprite(u.frame % 4, 0), -size, -size, color);
		g.popTransform();
	}

	public void draw4DirSpriteBody(Graphics g, Unit u, SpriteSheet baseSheet, SpriteSheet colorSheet, Color color,
			int size) {
		g.pushTransform();
		int dir = (int) Math.floor((u.heading + DMath.PIF / 4.f) / (DMath.PIF * .5f)) % 4;
		dir += 4;
		dir %= 4;
		int[] spriteMap = { 3, 0, 1, 2 };

		// g.drawString( dir + "a", 10, 10 );
		g.drawImage(baseSheet.getSprite(spriteMap[dir], u.frame % 4), -size, -size);
		// g.drawLine( 0, 0, DMath.cosf( u.heading ) * 30, DMath.sinf( u.heading ) * 30
		// );
		g.popTransform();
	}

	public void draw8DirSpriteBody(Graphics g, Unit u, SpriteSheet baseSheet, SpriteSheet colorSheet, Color color,
			int size) {
		g.pushTransform();
		int dir = (int) Math.floor((u.heading + DMath.PIF / 8.f) / (DMath.PIF * .25f)) % 8;
		dir += 8;
		dir %= 8;
		int[] spriteMap = { 0, 5, 4, 6, 7, 3, 1, 2 };

		// g.drawString( dir + "a", 10, 10 );
		g.drawImage(baseSheet.getSprite(u.frame, spriteMap[dir]), -size, -size);
		// g.drawImage( cs.l.theme.dir8.getSubImage( 10, 10 ), 0, 0 );
		// float dirAngle = dir * DMath.PIF * .25f;
		// g.drawLine( 0, 0, DMath.cosf( dirAngle ) * 30, DMath.sinf( dirAngle ) * 30 );
		g.popTransform();
	}

	public void drawBody(Graphics g, Unit u, boolean player, ClientState cs) {
		Color color = player ? playerColor : u.owner.team.getColor();

		switch (u.type.name) {
			case "LIGHT":
				drawSpriteBody(g, u, cs.l.theme.light, cs.l.theme.lightColor, color, 8);
				break;
			case "SCOUT":
				// draw4DirSpriteBody( g, u, cs.l.theme.dir4, null, color, 8 );
				// break;
			case "SHOTGUN":
				// draw8DirSpriteBody( g, u, cs.l.theme.dir8, null, color, 24 );
				// break;
			case "SABOTEUR":
				if (player) {
					g.setColor(Color.white);
					g.fillOval(-7, -7, 14, 14);
					g.setColor(Color.black);
					g.drawOval(-7, -7, 14, 14);
				}

				g.pushTransform();
				g.rotate(0, 0, (u.heading / DMath.PI2F * 360));
				g.setColor(color);
				g.fillOval(-5, -5, 10, 10);
				g.setColor(Color.black);
				g.drawOval(-5, -5, 10, 10);
				g.drawLine(0, 0, 5, 0);
				g.popTransform();

				break;
			case "SNIPER":
				if (player) {
					g.setColor(Color.white);
					g.fillOval(-7, -6, 14, 12);
					g.setColor(Color.black);
					g.drawOval(-7, -6, 14, 12);
				}

				g.pushTransform();
				g.rotate(0, 0, (u.heading / DMath.PI2F * 360));
				g.setColor(color);
				g.fillOval(-5, -4, 10, 8);
				g.setColor(Color.black);
				g.drawOval(-5, -4, 10, 8);
				g.drawLine(0, 0, 5, 0);
				g.popTransform();
				break;
			case "HEAVY":
				drawSpriteBody(g, u, cs.l.theme.heavy, cs.l.theme.heavyColor, color, 16);
				break;
		}
	}
}
