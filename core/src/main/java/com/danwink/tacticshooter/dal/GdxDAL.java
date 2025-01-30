package com.danwink.tacticshooter.dal;

import java.util.ArrayDeque;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.danwink.tacticshooter.ui.DUIScreen.Gdx2DEventMapper;
import com.phyloa.dlib.dui.DEventMapper;
import com.phyloa.dlib.math.Point2i;

import jp.objectclub.vecmath.Vector2f;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class GdxDAL extends DAL {
    InputMultiplexer inputMultiplexer;

    public GdxGraphics graphics;

    public static PausableRender currentRenderer = null;

    public void init() {
        inputMultiplexer = new InputMultiplexer();

        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public DALGraphics getGraphics() {
        if (graphics == null) {
            graphics = new GdxGraphics(() -> new Point2i(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        }

        return graphics;
    }

    @Override
    public int getWidth() {
        return Gdx.graphics.getWidth();
    }

    @Override
    public int getHeight() {
        return Gdx.graphics.getHeight();
    }

    @Override
    public DALTexture generateRenderableTexture(int width, int height) {
        return new GdxFboTexture(this, width, height);
    }

    @Override
    public void setForceExit(boolean forceExit) {

    }

    @Override
    public void exit() {

    }

    @Override
    public DAL useGraphics(DALGraphics g) {
        var newDAL = new GdxDAL();
        newDAL.inputMultiplexer = inputMultiplexer;
        newDAL.graphics = (GdxGraphics) g;

        return newDAL;
    }

    @Override
    public void setMusicVolume(float f) {
    }

    @Override
    public void setSoundVolume(float f) {
    }

    @Override
    public void setVSync(boolean b) {
    }

    @Override
    public void setFullscreen(boolean b) {
    }

    @Override
    public boolean isFullscreen() {
        return Gdx.graphics.isFullscreen();
    }

    @Override
    public DEventMapper getEventMapper() {
        var mapper = new Gdx2DEventMapper();
        inputMultiplexer.addProcessor(mapper);
        return mapper;
    }

    public interface PausableRender {
        void pauseRender();

        void resumeRender();
    }

    public static class GdxRegionTexture implements DALTexture {
        TextureRegion region;

        public GdxRegionTexture(TextureRegion region) {
            this.region = region;
        }

        @Override
        public int getWidth() {
            return region.getRegionWidth();
        }

        @Override
        public int getHeight() {
            return region.getRegionHeight();
        }

        @Override
        public void renderTo(Consumer<DALGraphics> consumer) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'renderTo'");
        }

        @Override
        public DALTexture getSubImage(int x, int y, int width, int height) {
            return new GdxRegionTexture(new TextureRegion(region, x, y, width, height));
        }

        @Override
        public DALColor getColor(int x, int y) {
            // This is probably crazy inefficient
            var pixmap = region.getTexture().getTextureData().consumePixmap();
            var tx = region.getRegionX() + x;
            var ty = region.getRegionY() + y;

            var color = new DALColor(pixmap.getPixel(tx, ty));
            pixmap.dispose();
            return color;
        }

        @Override
        public void flushPixelData() {
            // We could use this as an opportunity to cache the pixmap to make getColor more
            // efficient
        }

        @Override
        public TextureRegion getTextureRegion() {
            return region;
        }
    }

    public static class GdxFboTexture implements DALTexture, PausableRender {
        FrameBuffer fbo = null;
        TextureRegion fboRegion = null;
        private GdxDAL dal;
        private GdxGraphics g;

        public GdxFboTexture(GdxDAL dal, int width, int height) {
            this.dal = dal;
            fbo = new FrameBuffer(Format.RGBA8888, width, height, false);
            fboRegion = new TextureRegion(fbo.getColorBufferTexture());
            fboRegion.flip(false, true);

            this.g = new GdxGraphics(() -> new Point2i(fbo.getWidth(), fbo.getHeight()));
        }

        @Override
        public int getWidth() {
            return fbo.getWidth();
        }

        @Override
        public int getHeight() {
            return fbo.getHeight();
        }

        @Override
        public void renderTo(Consumer<DALGraphics> consumer) {
            var pausedRender = currentRenderer;
            if (pausedRender != null) {
                pausedRender.pauseRender();
            }

            currentRenderer = this;

            HdpiUtils.setMode(HdpiMode.Pixels);

            fbo.begin();
            g.preRender();
            consumer.accept(g);
            g.postRender();
            fbo.end();

            HdpiUtils.setMode(HdpiMode.Logical);

            if (pausedRender != null) {
                currentRenderer = pausedRender;
                pausedRender.resumeRender();
            } else {
                pausedRender = null;
            }
        }

        public void pauseRender() {
            g.pauseRender();
            fbo.end();
        }

        public void resumeRender() {
            fbo.begin();
            g.resumeRender();
        }

        @Override
        public DALTexture getSubImage(int x, int y, int width, int height) {
            return new GdxRegionTexture(new TextureRegion(fboRegion, x, y, width, height));
        }

        @Override
        public DALColor getColor(int x, int y) {
            // This is probably crazy inefficient
            var pixmap = fbo.getColorBufferTexture().getTextureData().consumePixmap();
            var color = new DALColor(pixmap.getPixel(x, y));
            pixmap.dispose();
            return color;
        }

        @Override
        public void flushPixelData() {

        }

        @Override
        public TextureRegion getTextureRegion() {
            return fboRegion;
        }
    }

    public static class GdxGraphics implements DALGraphics, PausableRender {
        private Supplier<Point2i> sizeSupplier;
        public OrthographicCamera camera;
        public Viewport viewport;
        OrthographicCamera uiCamera;
        Viewport uiViewport;
        public SpriteBatch batch;
        ShapeDrawer sd;

        BitmapFont font = new BitmapFont();
        GlyphLayout glyphLayout = new GlyphLayout();

        ArrayDeque<Matrix4> transformStack = new ArrayDeque<>();

        boolean isDrawing = false;

        public GdxGraphics(Supplier<Point2i> sizeSupplier) {
            this.sizeSupplier = sizeSupplier;

            camera = new OrthographicCamera();
            camera.setToOrtho(true);
            viewport = new ScreenViewport(camera);
            viewport.update(sizeSupplier.get().x, sizeSupplier.get().y, true);

            // camera.position.set(0, 0, 0);
            camera.zoom = 1;

            batch = new SpriteBatch();
            sd = new ShapeDrawer(batch, new TextureRegion(new Texture(Gdx.files.internal("assets/1x1_white.png"))));
        }

        public void preRender() {
            viewport.apply();
            camera.update();

            batch.setProjectionMatrix(camera.combined);
            batch.enableBlending();
            batch.begin();
            batch.setColor(1, 1, 1, 1);

            isDrawing = true;
            GdxDAL.currentRenderer = this;
        }

        public void postRender() {
            batch.end();

            isDrawing = false;
            GdxDAL.currentRenderer = null;
        }

        public void pauseRender() {
            if (isDrawing) {
                batch.end();
                isDrawing = false;
            }
        }

        public void resumeRender() {
            if (!isDrawing) {
                batch.begin();
                isDrawing = true;
            }
        }

        public void setFont(BitmapFont f) {
            font = f;
        }

        @Override
        public int getWidth() {
            return sizeSupplier.get().x;
        }

        @Override
        public int getHeight() {
            return sizeSupplier.get().y;
        }

        public TextureRegion getRegion(DALTexture texture) {
            if (texture instanceof GdxRegionTexture) {
                return ((GdxRegionTexture) texture).region;
            } else if (texture instanceof GdxFboTexture) {
                return ((GdxFboTexture) texture).fboRegion;
            }
            return null;
        }

        @Override
        public void drawImage(DALTexture texture, float x, float y) {
            var last = batch.getColor();
            batch.setColor(1, 1, 1, 1);
            batch.draw(getRegion(texture), x, y);
            batch.setColor(last);
        }

        @Override
        public void drawImage(DALTexture texture, float x, float y, DALColor color) {
            var last = batch.getColor();
            batch.setColor(color.r, color.g, color.b, color.a);
            batch.draw(getRegion(texture), x, y);
            batch.setColor(last);
        }

        @Override
        public void drawImage(DALTexture texture, float x, float y, float width, float height) {
            var last = batch.getColor();
            batch.setColor(1, 1, 1, 1);
            batch.draw(getRegion(texture), x, y, width, height);
            batch.setColor(last);
        }

        @Override
        public void drawImage(DALTexture texture, float x, float y, float width, float height, DALColor color) {
            var last = batch.getColor();
            batch.setColor(color.r, color.g, color.b, color.a);
            batch.draw(getRegion(texture), x, y, width, height);
            batch.setColor(last);
        }

        @Override
        public void drawImage(DALTexture texture, float x, float y, float x2, float y2, float srcX, float srcY,
                float srcx2, float srcy2) {
            var region = getRegion(texture);
            var toDrawRegion = new TextureRegion(region, (int) srcX, (int) srcY, (int) (srcx2 - srcX),
                    (int) (srcy2 - srcY));
            toDrawRegion.flip(false, true);
            batch.draw(toDrawRegion, x, y, x2 - x, y2 - y);
        }

        @Override
        public void drawImage(DALTexture texture, float x, float y, float x2, float y2, float srcX, float srcY,
                float srcx2, float srcy2, float alpha) {
            var last = batch.getColor();
            batch.setColor(1, 1, 1, alpha);
            var region = getRegion(texture);
            var toDrawRegion = new TextureRegion(region, (int) srcX, (int) srcY, (int) (srcx2 - srcX),
                    (int) (srcy2 - srcY));
            toDrawRegion.flip(false, true);
            batch.draw(toDrawRegion, x, y, x2 - x, y2 - y);
            batch.setColor(last);
        }

        @Override
        public void drawImage(DALTexture texture, float x, float y, float x2, float y2, float srcX, float srcY,
                float srcx2, float srcy2, DALColor color) {
            var last = batch.getColor();
            batch.setColor(color.r, color.g, color.b, color.a);
            var region = getRegion(texture);
            var toDrawRegion = new TextureRegion(region, (int) srcX, (int) srcY, (int) (srcx2 - srcX),
                    (int) (srcy2 - srcY));
            toDrawRegion.flip(false, true);
            batch.draw(toDrawRegion, x, y, x2 - x, y2 - y);
            batch.setColor(last);
        }

        @Override
        public void setAntiAlias(boolean b) {
        }

        @Override
        public void clear() {
        }

        @Override
        public void flush() {
            batch.flush();
        }

        @Override
        public void pushTransform() {
            transformStack.push(batch.getTransformMatrix().cpy());
        }

        @Override
        public void popTransform() {
            batch.setTransformMatrix(transformStack.pop());
        }

        @Override
        public void translate(float x, float y) {
            batch.setTransformMatrix(batch.getTransformMatrix().cpy().translate(x, y, 0));
        }

        @Override
        public void scale(float x, float y) {
            batch.setTransformMatrix(batch.getTransformMatrix().cpy().scale(x, y, 1));
        }

        @Override
        public void rotate(float angle) {
            batch.setTransformMatrix(batch.getTransformMatrix().cpy().rotate(0, 0, 1, angle));
        }

        @Override
        public void rotate(float x, float y, float angle) {
            batch.setTransformMatrix(
                    batch.getTransformMatrix().cpy().translate(x, y, 0).rotate(0, 0, 1, angle).translate(-x, -y, 0));
        }

        @Override
        public void setLineWidth(int width) {
        }

        @Override
        public void setColor(float r, float g, float b, float a) {
            sd.setColor(r, g, b, a);
        }

        @Override
        public void setColor(int c) {
            sd.setColor(c);
        }

        @Override
        public void setColor(DALColor color) {
            sd.setColor(color.r, color.g, color.b, color.a);
        }

        @Override
        public void fillRect(float x, float y, float width, float height) {
            sd.filledRectangle(x, y, width, height);
        }

        @Override
        public void drawRect(float x, float y, float width, float height) {
            sd.rectangle(x, y, width, height);
        }

        @Override
        public void fillRoundedRect(float x, float y, float width, float height, int cornerRadius, int segs) {
            sd.filledRectangle(x, y, width, height);
        }

        @Override
        public void drawRoundedRect(float x, float y, float width, float height, int cornerRadius, int segs) {
            sd.rectangle(x, y, width, height);
        }

        @Override
        public void fillOval(float x, float y, float width, float height) {
            sd.filledEllipse(x + width / 2, y + height / 2, width / 2, height / 2);
        }

        @Override
        public void drawOval(float x, float y, float width, float height) {
            sd.ellipse(x + width / 2, y + height / 2, width / 2, height / 2);
        }

        @Override
        public void setLineWidth(float width) {
            sd.setDefaultLineWidth(width);
        }

        @Override
        public void drawLine(float x1, float y1, float x2, float y2) {
            sd.line(x1, y1, x2, y2);
        }

        @Override
        public void drawText(String text, float x, float y) {
            font.draw(batch, text, x, y);
        }

        @Override
        public Vector2f getStringSize(String text) {
            glyphLayout.setText(font, text);
            return new Vector2f(glyphLayout.width, glyphLayout.height);
        }

        @Override
        public void setDrawMode(int mode) {
            // These settings are copied from the Slick2D renderer
            if (mode == DALGraphics.MODE_NORMAL) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            } else if (mode == DALGraphics.MODE_ADD) {
                batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
            } else if (mode == DALGraphics.MODE_COLOR_MULTIPLY) {
                batch.setBlendFunction(GL20.GL_ONE_MINUS_SRC_COLOR, GL20.GL_SRC_COLOR);
            }
        }

        @Override
        public void setClearColor(DALColor color) {
        }

        @Override
        public float getTextWidth(String message) {
            glyphLayout.setText(font, message);
            return glyphLayout.width;
        }

        @Override
        public void drawArc(float x, float y, float width, float height, float start, float end) {
        }

        @Override
        public void withClip(float x, float y, float width, float height, Runnable r) {
            Rectangle scissors = new Rectangle();
            Rectangle clipBounds = new Rectangle(x, y, width, height);
            ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);
            if (ScissorStack.pushScissors(scissors)) {
                r.run();
                ScissorStack.popScissors();
            }
        }
    }
}
