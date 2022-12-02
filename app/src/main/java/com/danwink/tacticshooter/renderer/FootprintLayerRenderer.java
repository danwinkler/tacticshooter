package com.danwink.tacticshooter.renderer;

import java.util.concurrent.ConcurrentLinkedDeque;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.phyloa.dlib.util.DMath;

public class FootprintLayerRenderer {
    public Color bloodColor = new Color(255, 0, 0);

    public Image texture;
    Graphics tg;

    private ConcurrentLinkedDeque<FootprintData> footprintToDraw = new ConcurrentLinkedDeque<>();

    int frames = 0;

    public void render(Graphics g, ClientState cs) {
        if (texture == null) {
            if (cs.l != null) {
                generateTexture(cs);
            } else {
                return;
            }
        }

        if (cs.mgs.input.isKeyPressed(Input.KEY_R)) {
            tg.clear();
        }

        tg.setDrawMode(Graphics.MODE_NORMAL);
        tg.setColor(new Color(.3f, .2f, .3f));
        while (!footprintToDraw.isEmpty()) {
            var fp = footprintToDraw.removeLast();
            tg.fillOval(fp.x, fp.y, fp.size, fp.size);
        }

        // Only fade out every few frames
        if (frames % 5 == 0) {
            tg.setDrawMode(Graphics.MODE_ADD);
            float fadeSpeed = .004f;
            tg.setColor(new Color(fadeSpeed, fadeSpeed, fadeSpeed));
            tg.fillRect(0, 0, texture.getWidth(), texture.getHeight());
            tg.setDrawMode(Graphics.MODE_NORMAL);
        }

        g.setDrawMode(Graphics.MODE_COLOR_MULTIPLY);
        g.drawImage(texture, 0, 0);
        g.setDrawMode(Graphics.MODE_NORMAL);

        frames++;
    }

    public void generateTexture(ClientState cs) {
        try {
            texture = new Image(cs.l.width * Level.tileSize, cs.l.height * Level.tileSize);
            tg = texture.getGraphics();
            tg.setBackground(new Color(1, 1, 1, 1f));
            tg.clear();
        } catch (SlickException e) {
            e.printStackTrace();
        }
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
