package com.danwink.tacticshooter;

import java.util.HashMap;

import com.danwink.tacticshooter.Theme.TSSpriteSheet;
import com.danwink.tacticshooter.dal.DAL.DALTexture;

public class AssetManager {
    public static HashMap<String, String> texturesToLoad = new HashMap<String, String>();
    public static HashMap<String, SpriteSheetDef> spriteSheetsToLoad = new HashMap<String, SpriteSheetDef>();

    public static HashMap<String, DALTexture> textures = new HashMap<String, DALTexture>();
    public static HashMap<String, TSSpriteSheet> spriteSheets = new HashMap<String, TSSpriteSheet>();

    public static AssetLoader loader;

    public static void defineTexture(String name, String path) {
        texturesToLoad.put(name, path);
    }

    public static class SpriteSheetDef {
        public String path;
        public int tw;
        public int th;

        public SpriteSheetDef(String path, int tw, int th) {
            this.path = path;
            this.tw = tw;
            this.th = th;
        }
    }

    public static void defineSpriteSheet(String name, String path, int tw, int th) {
        spriteSheetsToLoad.put(name, new SpriteSheetDef(path, tw, th));
    }

    public static DALTexture getTexture(String name) {
        return textures.get(name);
    }

    public static TSSpriteSheet getSpriteSheet(String name) {
        return spriteSheets.get(name);
    }

    public static void configureLoader(AssetLoader loader) {
        AssetManager.loader = loader;
    }

    public static void load() {
        for (String s : texturesToLoad.keySet()) {
            textures.put(s, loader.loadTexture(texturesToLoad.get(s)));
        }

        for (String s : spriteSheetsToLoad.keySet()) {
            SpriteSheetDef def = spriteSheetsToLoad.get(s);
            spriteSheets.put(s, loader.loadSpriteSheet(def.path, def.tw, def.th));
        }
    }

    public interface AssetLoader {
        public DALTexture loadTexture(String path);

        public TSSpriteSheet loadSpriteSheet(String path, int tw, int th);
    }
}
