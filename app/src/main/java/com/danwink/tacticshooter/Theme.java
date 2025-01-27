package com.danwink.tacticshooter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;

import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.dal.SlickDAL.SlickTexture;

public class Theme {
	static HashMap<String, Theme> themes = new HashMap<String, Theme>();

	String name;

	public DALTexture crater;
	public DALTexture grate;
	public DALTexture wall;
	public DALTexture floor;
	public DALTexture smoke;

	public TSSpriteSheet light;
	public TSSpriteSheet lightColor;
	public TSSpriteSheet heavy;
	public TSSpriteSheet heavyColor;
	public TSSpriteSheet dir4;
	public TSSpriteSheet dir8;
	public TSSpriteSheet flag;

	public Map<String, DALTexture> portraits = new HashMap<>();

	private Theme(String name) throws SlickException {
		this.name = name;
	}

	public void load() throws SlickException {
		crater = load("crater");
		grate = load("grate");
		wall = load("wall");
		floor = load("floor");
		smoke = load("smoke");

		light = loadSS("light", 16);
		lightColor = loadSS("light_color", 16);
		heavy = loadSS("heavy", 32);
		heavyColor = loadSS("heavy_color", 32);
		flag = loadSS("flag", 16);
	}

	public DALTexture getPortrait(String portrait) {
		if (!portraits.containsKey(portrait)) {
			try {
				portraits.put(portrait,
						new SlickTexture(new Image("data/themes/" + name + "/portraits/" + portrait + ".png")));
			} catch (SlickException e) {
				throw new RuntimeException(e);
			}
		}
		return portraits.get(portrait);
	}

	private DALTexture load(String s) throws SlickException {
		return new SlickTexture(new Image("data/themes" + File.separator + name + File.separator + s + ".png"));
	}

	private TSSpriteSheet loadSS(String s, int size) throws SlickException {
		return new SlickTSSpriteSheet(
				new SpriteSheet(new Image("data/themes" + File.separator + name + File.separator + s + ".png"), size,
						size));
	}

	public static Theme getTheme(String name) throws SlickException {
		Theme t = themes.get(name);
		if (t == null) {
			t = new Theme(name);
			themes.put(name, t);
		}
		return t;
	}

	public static abstract class TSSpriteSheet {
		public abstract SpriteSheet slim();

		public abstract DALTexture getSprite(int x, int y);
	}

	public static class SlickTSSpriteSheet extends TSSpriteSheet {
		public SpriteSheet ss;

		public SlickTSSpriteSheet(SpriteSheet ss) {
			this.ss = ss;
		}

		@Override
		public SpriteSheet slim() {
			return ss;
		}

		@Override
		public DALTexture getSprite(int x, int y) {
			return new SlickTexture(ss.getSubImage(x, y));
		}
	}
}
