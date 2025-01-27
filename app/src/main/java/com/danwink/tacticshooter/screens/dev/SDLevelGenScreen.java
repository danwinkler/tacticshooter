package com.danwink.tacticshooter.screens.dev;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.dom4j.DocumentException;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.imageout.ImageOut;

import com.danwink.tacticshooter.LevelFileHelper;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.SlickDAL;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.danwink.tacticshooter.ui.DUIScreen;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DColumnPanel;
import com.phyloa.dlib.dui.DDropDown;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.RelativePosition;

public class SDLevelGenScreen extends DUIScreen {
    DButton makeImages;
    DDropDown images;
    DButton convertImage;
    DButton back;

    Image test;

    @Override
    public void init(GameContainer gc) {
        makeImages = new DButton("Make Images");
        convertImage = new DButton("Convert Image");
        back = new DButton("Back");
        images = new DDropDown(0, 0, 0, 0);
    }

    @Override
    public void createUIElements(DUI dui, float windowHeight) {
        makeImages.setSize(200 * uiScale, 50 * uiScale);
        convertImage.setSize(200 * uiScale, 50 * uiScale);
        back.setSize(200 * uiScale, 50 * uiScale);
        images.setSize(800 * uiScale, 30 * uiScale);
        images.setItems(getImagePaths());

        DColumnPanel panel = new DColumnPanel(RelativePosition.CENTER, 0, 0);
        panel.add(makeImages);
        panel.add(images);
        panel.add(convertImage);
        panel.add(back);

        dui.add(panel);
    }

    @Override
    public void event(DUIEvent event) {
        if (event.getElement() == back) {
            dsh.activate("devmenu", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
        } else if (event.getElement() == makeImages) {
            makeImages();
        } else if (event.getElement() == convertImage) {
            convertImages();
        }
    }

    @Override
    public void update(GameContainer gc, float delta) {
        super.update(gc, delta);
    }

    @Override
    public void render(GameContainer gc, DAL dal) {
        super.render(gc, dal);

        var g = ((SlickDAL) dal).g;

        if (test != null) {
            g.drawImage(test, 0, 0);
        }
    }

    @Override
    public void message(Object o) {

    }

    public void makeImages() {
        var levelNames = LevelFileHelper.getLevelNames();
        for (int i = 0; i < levelNames.length; i++) {
            var name = levelNames[i];
            try {
                var level = LevelFileHelper.loadLevel(name);
                var im = convertLevelToSDTrainingImage(level);
                FileOutputStream fos = new FileOutputStream(
                        "screenshots/sdtrain/" + name + ".png");
                ImageOut.write(im.getFlippedCopy(false, false), "png", fos);
                fos.close();
                im.destroy();
            } catch (DocumentException | SlickException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Image convertLevelToSDTrainingImage(Level level) throws SlickException {
        Image im = new Image(512, 512);
        Graphics g = im.getGraphics();
        g.setAntiAlias(false);

        g.setColor(Color.white);
        g.fillRect(0, 0, 512, 512);

        float xScale = 512 / (float) level.width;
        float yScale = 512 / (float) level.height;

        g.setColor(Color.black);
        for (int x = 0; x < level.width; x++) {
            for (int y = 0; y < level.height; y++) {
                var tile = level.getTile(x, y);
                if (tile == TileType.WALL) {
                    g.fillRect(x * xScale, y * yScale, xScale, yScale);
                } else if (tile == TileType.GRATE) {
                    g.setColor(Color.gray);
                    g.fillRect(x * xScale, y * yScale, xScale, yScale);
                    g.setColor(Color.black);
                }
            }
        }

        for (var b : level.buildings) {
            float bx = ((b.x + .5f) / Level.tileSize) * xScale;
            float by = ((b.y + .5f) / Level.tileSize) * yScale;
            float buildingRadius = 10;

            if (b.bt == BuildingType.CENTER) {
                g.setColor(Color.red);
            } else if (b.bt == BuildingType.POINT) {
                g.setColor(Color.green);
            }

            g.fillOval(bx - buildingRadius, by - buildingRadius, buildingRadius * 2, buildingRadius * 2);
        }

        g.flush();

        return im;
    }

    public static String[] getImagePaths() {
        return Arrays.stream(new File("screenshots/sdgenerated").listFiles()).map(f -> f.toString())
                .toArray(String[]::new);
    }

    public void convertImages() {
        dsh.message("sdimageparse", images.getSelected());
        dsh.activate("sdimageparse", gc);
    }
}
