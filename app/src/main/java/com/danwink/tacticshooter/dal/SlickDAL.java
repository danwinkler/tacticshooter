package com.danwink.tacticshooter.dal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.phyloa.dlib.math.Point2i;

import jp.objectclub.vecmath.Vector2f;

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
            slickGraphics = new SlickGraphics(g, () -> new Point2i(gc.getWidth(), gc.getHeight()));
        }
        return slickGraphics;
    }

    @Override
    public DALTexture generateRenderableTexture(int width, int height) {
        return new SlickTexture(width, height);
    }

    public static class SlickGraphics implements DALGraphics {
        public Graphics g;
        private Supplier<Point2i> sizeProvider;

        public SlickGraphics(Graphics g, Supplier<Point2i> sizeProvider) {
            this.g = g;
            this.sizeProvider = sizeProvider;
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

        public void drawImage(DALTexture texture, float x, float y, float x2, float y2, float srcX, float srcY,
                float srcx2, float srcy2, float alpha) {
            SlickTexture t = (SlickTexture) texture;
            g.drawImage(t.image, x, y, x2, y2, srcX, srcY, srcx2, srcy2, new Color(1, 1, 1, alpha));
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

        @Override
        public int getWidth() {
            return sizeProvider.get().x;
        }

        @Override
        public int getHeight() {
            return sizeProvider.get().y;
        }

        @Override
        public void scale(float x, float y) {
            g.scale(x, y);
        }

        @Override
        public void rotate(float angle) {
            g.rotate(0, 0, angle);
        }

        @Override
        public void setColor(float r, float g, float b, float a) {
            this.g.setColor(new Color(r, g, b, a));
        }

        @Override
        public void setColor(int c) {
            this.g.setColor(new Color(c));
        }

        @Override
        public void setColor(Color color) {
            this.g.setColor(color);
        }

        @Override
        public void fillRect(float x, float y, float width, float height) {
            g.fillRect(x, y, width, height);
        }

        @Override
        public void drawRect(float x, float y, float width, float height) {
            g.drawRect(x, y, width, height);
        }

        @Override
        public void fillRoundedRect(float x, float y, float width, float height, int cornerRadius, int segs) {
            g.fillRoundRect(x, y, width, height, cornerRadius, segs);
        }

        @Override
        public void drawRoundedRect(float x, float y, float width, float height, int cornerRadius, int segs) {
            g.drawRoundRect(x, y, width, height, cornerRadius, segs);
        }

        @Override
        public void fillOval(float x, float y, float width, float height) {
            g.fillOval(x, y, width, height);
        }

        @Override
        public void drawOval(float x, float y, float width, float height) {
            g.drawOval(x, y, width, height);
        }

        @Override
        public void drawLine(float x1, float y1, float x2, float y2) {
            g.drawLine(x1, y1, x2, y2);
        }

        @Override
        public void drawText(String text, float x, float y) {
            g.drawString(text, x, y);
        }

        @Override
        public Vector2f getStringSize(String text) {
            Vector2f v = new Vector2f();
            org.newdawn.slick.Font f = g.getFont();
            v.x = f.getWidth(text);
            v.y = f.getHeight(text);
            return v;
        }

        @Override
        public void setLineWidth(float width) {
            g.setLineWidth(width);
        }

        @Override
        public void setClip(int x, int y, int width, int height) {
            g.setClip(x, y, width, height);
        }

        @Override
        public void clearClip() {
            g.clearClip();
        }

        @Override
        public void drawImage(DALTexture texture, float x, float y, DALColor color) {
            SlickTexture t = (SlickTexture) texture;
            g.drawImage(t.image, x, y, new Color(color.r, color.g, color.b, color.a));
        }

        @Override
        public void rotate(float x, float y, float angle) {
            g.rotate(x, y, angle);
        }

        @Override
        public void setColor(DALColor color) {
            g.setColor(new Color(color.r, color.g, color.b, color.a));
        }

        @Override
        public void drawImage(DALTexture texture, float x, float y, float x2, float y2, float srcX, float srcY,
                float srcx2, float srcy2, DALColor color) {
            SlickTexture t = (SlickTexture) texture;
            g.drawImage(t.image, x, y, x2, y2, srcX, srcY, srcx2, srcy2, new Color(color.r, color.g, color.b, color.a));
        }

        @Override
        public void setDrawMode(int mode) {
            g.setDrawMode(mode);
        }

        @Override
        public void setClearColor(DALColor color) {
            g.setBackground(new Color(color.r, color.g, color.b, color.a));
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
                consumer.accept(
                        new SlickGraphics(image.getGraphics(), () -> new Point2i(image.getWidth(), image.getHeight())));
            } catch (SlickException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * TODO: remove this before cutover
         */
        public Image slim() {
            return image;
        }

        @Override
        public DALTexture getSubImage(int x, int y, int width, int height) {
            return new SlickTexture(image.getSubImage(x, y, width, height));
        }

        @Override
        public DALColor getColor(int x, int y) {
            Color c = image.getColor(x, y);
            return new DALColor(c.r, c.g, c.b, c.a);
        }
    }

}
