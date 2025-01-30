package com.danwink.tacticshooter;

import java.util.HashMap;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.danwink.tacticshooter.Theme.GdxSpriteSheet;
import com.danwink.tacticshooter.Theme.TSSpriteSheet;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.dal.GdxDAL.GdxRegionTexture;

public class Assets {
    public static HashMap<String, String> keyToPath = new HashMap<String, String>();

    public static HashMap<String, SpriteSheetDef> spriteSheetData = new HashMap<String, SpriteSheetDef>();

    public static AssetManager manager = new AssetManager();

    public static void defineTexture(String name, String path) {
        keyToPath.put(name, path);
        manager.load(path, Texture.class);
    }

    public static void defineMusic(String name, String path) {
        keyToPath.put(name, path);
        manager.load(path, Music.class);
    }

    public static void defineSound(String name, String path) {
        keyToPath.put(name, path);
        manager.load(path, Sound.class);
    }

    public static class SpriteSheetDef {
        public int tw;
        public int th;

        public SpriteSheetDef(int tw, int th) {
            this.tw = tw;
            this.th = th;
        }
    }

    public static void defineSpriteSheet(String name, String path, int tw, int th) {
        spriteSheetData.put(name, new SpriteSheetDef(tw, th));
        keyToPath.put(name, path);
        manager.load(path, Texture.class);
    }

    public static DALTexture getTexture(String name) {
        var texture = manager.get(keyToPath.get(name), Texture.class);
        var region = new TextureRegion(texture);
        region.flip(false, true);
        return new GdxRegionTexture(region);
    }

    public static TSSpriteSheet getSpriteSheet(String name) {
        var texture = manager.get(keyToPath.get(name), Texture.class);
        var def = spriteSheetData.get(name);
        return new GdxSpriteSheet(texture, def.tw, def.th);
    }

    public static Music getMusic(String name) {
        return manager.get(keyToPath.get(name), Music.class);
    }

    public static Sound getSound(String name) {
        return manager.get(keyToPath.get(name), Sound.class);
    }
}
