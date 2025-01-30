package com.danwink.tacticshooter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.dal.GdxDAL.GdxRegionTexture;

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

	private Theme(String name) {
		this.name = name;
	}

	public void load() {
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
			portraits.put(portrait, new GdxRegionTexture(new TextureRegion(new Texture(Gdx.files.internal(
					"data/themes" + File.separator + name + File.separator + "portraits" + File.separator + portrait
							+ ".png")))));
		}
		return portraits.get(portrait);
	}

	private DALTexture load(String s) {
		var tex = new Texture(Gdx.files.internal(
				"data/themes" + File.separator + name + File.separator + s + ".png"));
		tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		var tr = new TextureRegion(tex);
		// tr.flip(false, true);
		return new GdxRegionTexture(tr);
	}

	private TSSpriteSheet loadSS(String s, int size) {
		return new GdxSpriteSheet(new Texture(Gdx.files.internal(
				"data/themes" + File.separator + name + File.separator + s + ".png")), size,
				size);
	}

	public static Theme getTheme(String name) {
		Theme t = themes.get(name);
		if (t == null) {
			t = new Theme(name);
			themes.put(name, t);
		}
		return t;
	}

	public static abstract class TSSpriteSheet {
		public abstract DALTexture getSprite(int x, int y);
	}

	public static class GdxSpriteSheet extends TSSpriteSheet {
		public Texture t;
		public DALTexture[][] dt;

		public GdxSpriteSheet(Texture t, int tw, int th) {
			this.t = t;
			var tr = TextureRegion.split(t, tw, th);
			dt = new DALTexture[tr.length][tr[0].length];
			for (int y = 0; y < tr.length; y++) {
				for (int x = 0; x < tr[y].length; x++) {
					tr[y][x].flip(false, true);
					dt[y][x] = new GdxRegionTexture(tr[y][x]);
				}
			}
		}

		@Override
		public DALTexture getSprite(int x, int y) {
			return dt[y][x];
		}
	}
}
