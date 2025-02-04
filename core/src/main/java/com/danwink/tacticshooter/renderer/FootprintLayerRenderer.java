package com.danwink.tacticshooter.renderer;

import java.util.concurrent.ConcurrentLinkedDeque;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.DAL.DALGraphics;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.phyloa.dlib.util.DMath;

public class FootprintLayerRenderer {
    public DALColor bloodColor = new DALColor(255, 0, 0);

    public DALTexture texture;

    private ConcurrentLinkedDeque<FootprintData> footprintToDraw = new ConcurrentLinkedDeque<>();

    int frames = 0;

    public void render(DAL dal, ClientState cs) {
        if (texture == null) {
            if (cs.l != null) {
                generateTexture(dal, cs);
            } else {
                return;
            }
        }

        texture.renderTo(tg -> {
            if (Gdx.input.isKeyPressed(Keys.R)) {
                tg.clear();
            }

            tg.setDrawMode(DALGraphics.MODE_NORMAL);
            tg.setColor(new DALColor(.3f, .2f, .3f));
            while (!footprintToDraw.isEmpty()) {
                var fp = footprintToDraw.removeLast();
                tg.fillOval(fp.x, fp.y, fp.size, fp.size);
            }

            // Only fade out every few frames
            if (frames % 5 == 0) {
                tg.setDrawMode(DALGraphics.MODE_ADD);
                float fadeSpeed = .004f;
                tg.setColor(new DALColor(fadeSpeed, fadeSpeed, fadeSpeed));
                tg.fillRect(0, 0, texture.getWidth(), texture.getHeight());
                tg.setDrawMode(DALGraphics.MODE_NORMAL);
            }
        });

        var g = dal.getGraphics();

        g.setDrawMode(DALGraphics.MODE_COLOR_MULTIPLY);
        g.drawImage(texture, 0, 0);
        g.setDrawMode(DALGraphics.MODE_NORMAL);

        frames++;
    }

    public void generateTexture(DAL dal, ClientState cs) {
        texture = dal.generateRenderableTexture(cs.l.width * Level.tileSize, cs.l.height * Level.tileSize);
        texture.getTextureRegion().flip(false, true);
        texture.renderTo(tg -> {
            tg.setClearColor(new DALColor(1, 1, 1, 1f));
            tg.clear();
            tg.setColor(DALColor.white);
            tg.fillRect(0, 0, texture.getWidth(), texture.getHeight());
        });
    }

    public void unitFrameUpdate(Unit unit) {
        var frame = unit.frame % 4;
        if (frame == 1 || frame == 3) {
            float headingOffset = frame == 1 ? -DMath.PIF / 2 : DMath.PIF / 2;
            float footprintOffset = 3;
            var footprintData = new FootprintData();
            footprintData.x = unit.x + DMath.cosf(unit.heading + headingOffset) * footprintOffset;
            footprintData.y = unit.y + DMath.sinf(unit.heading + headingOffset) * footprintOffset;
            footprintData.heading = unit.heading;
            footprintData.size = 4;
            footprintToDraw.add(footprintData);
        }
    }

    public static class FootprintData {
        public float x, y;
        public float heading;
        public float size;
    }
}
