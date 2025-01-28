package com.danwink.tacticshooter.screens.dev;

import java.util.ArrayList;

import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.DAL.DALGraphics;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.ui.DUIScreen;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.math.Point2i;

public class SpriteEditor extends DUIScreen {

    DALTexture image;

    @Override
    public void init(DAL dal) {

    }

    @Override
    public void render(DAL dal) {
        var g = dal.getGraphics();

        if (image == null) {
            image = dal.generateRenderableTexture(32, 32);
        }

        float armOffset = 9;
        float armRadius = 6;

        float bodyRadius = 8.5f;

        image.renderTo(ig -> {
            ig.clear();

            ig.pushTransform();
            ig.setAntiAlias(false);
            ig.setDrawMode(DALGraphics.MODE_NORMAL);
            ig.translate(image.getWidth() / 2, image.getHeight() / 2);
            ig.setColor(DALColor.white);
            ig.fillOval(armOffset - armRadius, -armRadius, armRadius * 2, armRadius * 2);
            ig.fillOval(-armOffset - armRadius, -armRadius, armRadius * 2, armRadius * 2);
            ig.fillOval(-bodyRadius, -bodyRadius, bodyRadius * 2, bodyRadius * 2);

            // Erase a bit off the back
            ig.setDrawMode(DALGraphics.MODE_COLOR_MULTIPLY_ALPHA);
            ig.setColor(DALColor.black);
            ig.fillRect(-bodyRadius, 6, bodyRadius * 2, 5);

            ig.setDrawMode(DALGraphics.MODE_NORMAL);

            ig.popTransform();

            ig.flush();
            image.flushPixelData();

            outline(ig, image.getWidth(), image.getHeight());
        });

        g.pushTransform();
        g.translate(dal.getWidth() / 2, dal.getHeight() / 2);
        g.scale(10, 10);

        g.drawImage(image, 0, 0);

        g.popTransform();

        // Render ui
        super.render(dal);
    }

    public void outline(DALGraphics g, int width, int height) {
        ArrayList<Point2i> pixelsToDraw = new ArrayList<Point2i>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // If pixel is white, and any neighbor is transparent, draw a black pixel
                if (image.getColor(x, y).a == 1.f) {
                    var left = x > 0 ? image.getColor(x - 1, y).a == 0 : true;
                    var right = x < width - 1 ? image.getColor(x + 1, y).a == 0 : true;
                    var up = y > 0 ? image.getColor(x, y - 1).a == 0.f : true;
                    var down = y < height - 1 ? image.getColor(x, y + 1).a == 0 : true;
                    if (left || right || up || down) {
                        pixelsToDraw.add(new Point2i(x, y));
                    }
                }
            }
        }

        g.setColor(DALColor.black);
        for (var p : pixelsToDraw) {
            g.drawLine(p.x, p.y, p.x, p.y);
        }
        g.flush();
    }

    @Override
    public void createUIElements(DUI dui, float windowHeight) {

    }

    @Override
    public void event(DUIEvent event) {

    }

    @Override
    public void message(Object o) {

    }
}
