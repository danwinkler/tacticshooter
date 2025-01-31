package com.danwink.tacticshooter.dal;

import java.awt.Color;
import java.awt.Font;
import java.util.function.Consumer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.phyloa.dlib.dui.DEventMapper;
import com.phyloa.dlib.renderer.Renderer2D;

import jp.objectclub.vecmath.Vector2f;

/**
 * DAL is the Dan Abstraction Layer.
 * 
 * We're going to use it to cut over from Slick2D to LibGDX.
 * 
 * The primary architectural guidelines here are:
 * 1. Choose API design that makes building implementations for Slick2D and
 * LibGDX easy.
 * 2. The default implementations should be "No-op" such that a partial
 * implementation will still run.
 */
public abstract class DAL {
    public abstract DALGraphics getGraphics();

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract DALTexture generateRenderableTexture(int width, int height);

    public abstract void setForceExit(boolean forceExit);

    public abstract void exit();

    public abstract DAL useGraphics(DALGraphics g);

    public abstract void setMusicVolume(float f);

    public abstract void setSoundVolume(float f);

    public abstract void setVSync(boolean b);

    public abstract void setFullscreen(boolean b);

    public abstract boolean isFullscreen();

    public abstract DEventMapper getEventMapper();

    public interface DALGraphics {
        public static int MODE_NORMAL = 1;
        public static int MODE_ALPHA_MAP = 2;
        public static int MODE_ALPHA_BLEND = 3;
        public static int MODE_COLOR_MULTIPLY = 4;
        public static int MODE_ADD = 5;
        public static int MODE_SCREEN = 6;
        public static int MODE_ADD_ALPHA = 7;
        public static int MODE_COLOR_MULTIPLY_ALPHA = 8;

        public void preRender();

        public void postRender();

        public int getWidth();

        public int getHeight();

        public void drawImage(DALTexture texture, float x, float y);

        public void drawImage(DALTexture texture, float x, float y, DALColor color);

        public void drawImage(DALTexture texture, float x, float y, float width, float height);

        public void drawImage(DALTexture texture, float x, float y, float width, float height, DALColor color);

        public void drawImage(DALTexture texture, float x, float y, float x2, float y2, float srcX, float srcY,
                float srcx2, float srcy2);

        public void drawImage(DALTexture texture, float x, float y, float x2, float y2, float srcX, float srcY,
                float srcx2, float srcy2, float alpha);

        public void drawImage(DALTexture texture, float x, float y, float x2, float y2, float srcX, float srcY,
                float srcx2, float srcy2, DALColor color);

        public void setAntiAlias(boolean b);

        public void clear();

        public void flush();

        public void pushTransform();

        public void popTransform();

        public void translate(float x, float y);

        public void scale(float x, float y);

        public void rotate(float angle);

        public void rotate(float x, float y, float angle);

        public void setLineWidth(int width);

        public void setColor(float r, float g, float b, float a);

        public void setColor(int c);

        public void setColor(DALColor color);

        public void fillRect(float x, float y, float width, float height);

        public void drawRect(float x, float y, float width, float height);

        public void fillRoundedRect(float x, float y, float width, float height, int cornerRadius, int segs);

        public void drawRoundedRect(float x, float y, float width, float height, int cornerRadius, int segs);

        public void fillOval(float x, float y, float width, float height);

        public void drawOval(float x, float y, float width, float height);

        public void setLineWidth(float width);

        public void drawLine(float x1, float y1, float x2, float y2);

        public void drawText(String text, float x, float y);

        public Vector2f getStringSize(String text);

        public void setDrawMode(int mode);

        public void setClearColor(DALColor color);

        public float getTextWidth(String message);

        public void drawArc(float x, float y, float width, float height, float start, float end);

        public void setFont(BitmapFont f);

        public void withClip(float x, float y, float width, float height, Runnable r);
    }

    public interface DALTexture {
        public int getWidth();

        public int getHeight();

        public void renderTo(Consumer<DALGraphics> consumer);

        public DALTexture getSubImage(int x, int y, int width, int height);

        public DALColor getColor(int x, int y);

        public void flushPixelData();

        public TextureRegion getTextureRegion();

        public void saveToFile(FileHandle handle);
    }

    public static class DALColor {
        public static final DALColor transparent = new DALColor(0.0F, 0.0F, 0.0F, 0.0F);
        public static final DALColor white = new DALColor(1.0F, 1.0F, 1.0F, 1.0F);
        public static final DALColor yellow = new DALColor(1.0F, 1.0F, 0.0F, 1.0F);
        public static final DALColor red = new DALColor(1.0F, 0.0F, 0.0F, 1.0F);
        public static final DALColor blue = new DALColor(0.0F, 0.0F, 1.0F, 1.0F);
        public static final DALColor green = new DALColor(0.0F, 1.0F, 0.0F, 1.0F);
        public static final DALColor black = new DALColor(0.0F, 0.0F, 0.0F, 1.0F);
        public static final DALColor gray = new DALColor(0.5F, 0.5F, 0.5F, 1.0F);
        public static final DALColor cyan = new DALColor(0.0F, 1.0F, 1.0F, 1.0F);
        public static final DALColor darkGray = new DALColor(0.3F, 0.3F, 0.3F, 1.0F);
        public static final DALColor lightGray = new DALColor(0.7F, 0.7F, 0.7F, 1.0F);
        public static final DALColor pink = new DALColor(255, 175, 175, 255);
        public static final DALColor orange = new DALColor(255, 200, 0, 255);
        public static final DALColor magenta = new DALColor(255, 0, 255, 255);

        public float r, g, b, a;

        public DALColor(float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        public DALColor(int r, int g, int b, int a) {
            this.r = r / 255.f;
            this.g = g / 255.f;
            this.b = b / 255.f;
            this.a = a / 255.f;
        }

        public DALColor(int r, int g, int b) {
            this(r, g, b, 255);
        }

        public DALColor(float r, float g, float b) {
            this(r, g, b, 1.f);
        }

        public DALColor(int c) {
            this.r = ((c >> 16) & 0xFF) / 255.f;
            this.g = ((c >> 8) & 0xFF) / 255.f;
            this.b = (c & 0xFF) / 255.f;
            this.a = ((c >> 24) & 0xFF) / 255.f;
        }
    }

    public static Renderer2D<DALTexture> getDUIRenderer(DALGraphics g) {
        return new DALRenderer2D(g);
    }

    public static class DALRenderer2D implements Renderer2D<DALTexture> {
        DALGraphics g;

        public DALRenderer2D(DALGraphics g) {
            this.g = g;
        }

        @Override
        public void color(int c) {
            g.setColor(c);
        }

        @Override
        public void color(float r, float g, float b) {
            this.g.setColor(r / 255, g / 255, b / 255, 1);
        }

        @Override
        public void color(float r, float g, float b, float a) {
            this.g.setColor(r / 255, g / 255, b / 255, a / 255);
        }

        @Override
        public void color(Color color) {
            this.g.setColor(color.getRed() / 255.f, color.getGreen() / 255.f, color.getBlue() / 255.f,
                    color.getAlpha() / 255.f);
        }

        @Override
        public void line(float x1, float y1, float x2, float y2) {
            g.drawLine(x1, y1, x2, y2);
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
        public void fillRoundedRect(float x, float y, float width, float height, float arcWidth, float arcHeight) {
            g.fillRoundedRect(x, y, width, height, (int) arcWidth, 16);
        }

        @Override
        public void drawRoundedRect(float x, float y, float width, float height, float arcWidth, float arcHeight) {
            g.drawRoundedRect(x, y, width, height, (int) arcWidth, 16);
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
        public void text(String text, float x, float y) {
            g.drawText(text, x, y);
        }

        @Override
        public void translate(float x, float y) {
            g.translate(x, y);
        }

        @Override
        public void scale(float x, float y) {
            g.scale(x, y);
        }

        @Override
        public void rotate(float angle) {
            g.rotate(angle);
        }

        @Override
        public void pushMatrix() {
            g.pushTransform();
        }

        @Override
        public void popMatrix() {
            g.popTransform();
        }

        @Override
        public void drawImage(DALTexture img, float x, float y) {
            g.drawImage(img, x, y);
        }

        @Override
        public void drawImage(DALTexture img, float x, float y, float width, float height) {
            g.drawImage(img, x, y, width, height);
        }

        @Override
        public void drawImage(DALTexture img, float dx1, float dy1, float dx2, float dy2, float sx1, float sy1,
                float sx2, float sy2) {
            g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2);
        }

        @Override
        public int getWidth() {
            return g.getWidth();
        }

        @Override
        public int getHeight() {
            return g.getHeight();
        }

        @Override
        public Vector2f getStringSize(String text) {
            return g.getStringSize(text);
        }

        @Override
        public void setFont(Font font) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setFont'");
        }

        @Override
        public void setLineWidth(float width) {
            g.setLineWidth(width);
        }

        public void drawImage(DALTexture img, float dx1, float dy1, float dx2, float dy2, float sx1, float sy1,
                float sx2, float sy2, float alpha) {
            this.g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, alpha);
        }

        @Override
        public void withClip(float x, float y, float width, float height, Runnable r) {
            g.withClip(x, y, width, height, r);
        }

        @Override
        public DALGraphics getGraphics() {
            return g;
        }

        @Override
        public void drawImage(DALTexture img, float x, float y, float width, float height, DALColor color) {
            g.drawImage(img, x, y, width, height, color);
        }
    }
}
