package com.danwink.tacticshooter.dal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class SlickDAL extends DAL {
    public static Map<Image, SlickTexture> imageToTexture = new HashMap<>();

    public static SlickTexture getTexture(Image image) {
        if (!imageToTexture.containsKey(image)) {
            imageToTexture.put(image, new SlickTexture(image));
        }

        return imageToTexture.get(image);
    }

    public GameContainer gc;
    public Graphics g;

    private SlickGraphics slickGraphics;

    public SlickDAL() {
    }

    public SlickDAL(GameContainer gc, Graphics g) {
        this.gc = gc;
        this.g = g;
    }

    @Override
    public int getWidth() {
        return gc.getWidth();
    }

    @Override
    public int getHeight() {
        return gc.getHeight();
    }

    @Override
    public DALGraphics getGraphics() {
        if (slickGraphics == null) {
            slickGraphics = new SlickGraphics(g);
        }
        return slickGraphics;
    }

    @Override
    public DALTexture generateRenderableTexture(int width, int height) {
        return new SlickTexture(width, height);
    }

    public static class SlickGraphics implements DALGraphics {
        public Graphics g;

        public SlickGraphics(Graphics g) {
            this.g = g;
        }

        public void drawImage(DALTexture texture, float x, float y) {
            SlickTexture t = (SlickTexture) texture;
            g.drawImage(t.image, x, y);
        }

        public void drawImage(DALTexture texture, float x, float y, float width, float height) {
            SlickTexture t = (SlickTexture) texture;
            g.drawImage(t.image, x, y, x + width, y + height, 0, 0, t.image.getWidth(), t.image.getHeight());
        }

        public void drawImage(DALTexture texture, float x, float y, float x2, float y2, float srcX, float srcY,
                float srcx2, float srcy2) {
            SlickTexture t = (SlickTexture) texture;
            g.drawImage(t.image, x, y, x2, y2, srcX, srcY, srcx2, srcy2);
        }

        public void setAntiAlias(boolean b) {
            g.setAntiAlias(b);
        }

        public void clear() {
            g.clear();
            g.clearAlphaMap();
        }

        public void flush() {
            g.flush();
        }

        public void pushTransform() {
            g.pushTransform();
        }

        public void popTransform() {
            g.popTransform();
        }

        public void translate(float x, float y) {
            g.translate(x, y);
        }

        public void setLineWidth(int width) {
            g.setLineWidth(width);
        }
    }

    public static class SlickTexture implements DALTexture {
        public Image image;

        public SlickTexture(int width, int height) {
            try {
                this.image = new Image(width, height);
            } catch (SlickException e) {
                throw new RuntimeException(e);
            }
        }

        public SlickTexture(Image image) {
            this.image = image;
        }

        public int getWidth() {
            return image.getWidth();
        }

        public int getHeight() {
            return image.getHeight();
        }

        @Override
        public void renderTo(Consumer<DALGraphics> consumer) {
            try {
                consumer.accept(new SlickGraphics(image.getGraphics()));
            } catch (SlickException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
