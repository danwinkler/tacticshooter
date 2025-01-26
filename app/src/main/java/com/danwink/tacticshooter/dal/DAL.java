package com.danwink.tacticshooter.dal;

import java.util.function.Consumer;

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

    public interface DALGraphics {
        public void drawImage(DALTexture texture, float x, float y);

        public void drawImage(DALTexture texture, float x, float y, float width, float height);

        public void drawImage(DALTexture texture, float x, float y, float x2, float y2, float srcX, float srcY,
                float srcx2, float srcy2);

        public void setAntiAlias(boolean b);

        public void clear();

        public void flush();

        public void pushTransform();

        public void popTransform();

        public void translate(float x, float y);

        public void setLineWidth(int width);
    }

    public interface DALTexture {
        public int getWidth();

        public int getHeight();

        public void renderTo(Consumer<DALGraphics> consumer);
    }
}
