package com.danwink.tacticshooter.slick;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import jp.objectclub.vecmath.Vector2f;

public class Slick2DCamera {
    public float zoom;
    public float x, y;
    public float minZoom = 0.25f;
    public float maxZoom = 3.f;

    public Slick2DCamera() {
        zoom = 1;
        x = 0;
        y = 0;
    }

    public void translate(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public void zoom(float z) {
        zoom += z;
        if (zoom < minZoom) {
            zoom = minZoom;
        }
        if (zoom > maxZoom) {
            zoom = maxZoom;
        }
    }

    public void setZoom(float z) {
        zoom = z;
    }

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void start(GameContainer gc, Graphics g) {
        g.pushTransform();
        g.translate(gc.getWidth() / 2, gc.getHeight() / 2);
        g.scale(zoom, zoom);
        g.translate(-x, -y);
    }

    public void end(Graphics g) {
        g.popTransform();
    }

    public void reset() {
        zoom = 1;
        x = 0;
        y = 0;
    }

    public Vector2f screenToWorld(float x, float y, GameContainer gc) {
        return new Vector2f((x - gc.getWidth() / 2) / zoom + this.x, (y - gc.getHeight() / 2) / zoom + this.y);
    }
}
