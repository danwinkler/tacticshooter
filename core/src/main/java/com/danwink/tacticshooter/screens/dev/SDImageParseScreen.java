package com.danwink.tacticshooter.screens.dev;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;

import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.LevelFileHelper;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.Theme;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.dal.SlickDAL.SlickTexture;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Team;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.danwink.tacticshooter.renderer.GameRenderer;
import com.danwink.tacticshooter.screens.MultiplayerGameScreen;
import com.danwink.tacticshooter.ui.DUIScreen;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DCheckBox;
import com.phyloa.dlib.dui.DColumnPanel;
import com.phyloa.dlib.dui.DDropDown;
import com.phyloa.dlib.dui.DRowPanel;
import com.phyloa.dlib.dui.DText;
import com.phyloa.dlib.dui.DTextBox;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.RelativePosition;
import com.phyloa.dlib.math.Point2i;

import jp.objectclub.vecmath.Point2f;

public class SDImageParseScreen extends DUIScreen {
    DDropDown mirrorMode;
    DCheckBox addBorder;
    DTextBox width;
    DTextBox height;
    DButton generate;
    DButton save;
    DButton back;

    DALTexture image;
    DALTexture intermediate;

    Level level;
    ClientState cs;
    DALTexture levelImage;
    String name;

    @Override
    public void init(DAL gc) {
        mirrorMode = new DDropDown(0, 0, 0, 0);
        mirrorMode.addItems("None", "X", "Y", "XY");

        addBorder = new DCheckBox(0, 0, 0, 0);

        width = new DTextBox(0, 0, 0, 0);
        height = new DTextBox(0, 0, 0, 0);
        width.setText("63");
        height.setText("63");

        generate = new DButton("Generate");
        save = new DButton("Save");
        back = new DButton("Back");
    }

    @Override
    public void createUIElements(DUI dui, float windowHeight) {

        mirrorMode.setSize(200 * uiScale, 50 * uiScale);
        addBorder.setSize(50 * uiScale, 50 * uiScale);
        width.setSize(200 * uiScale, 50 * uiScale);
        height.setSize(200 * uiScale, 50 * uiScale);
        generate.setSize(200 * uiScale, 50 * uiScale);
        save.setSize(200 * uiScale, 50 * uiScale);
        back.setSize(200 * uiScale, 50 * uiScale);

        DColumnPanel panel = new DColumnPanel(RelativePosition.TOP_LEFT, 0, 0);

        panel.add(surroundWithLabel("Mirror Mode:", mirrorMode));
        panel.add(surroundWithLabel("Add Border:", addBorder));
        panel.add(surroundWithLabel("Width:", width));
        panel.add(surroundWithLabel("Height:", height));
        panel.add(generate);
        panel.add(save);
        panel.add(back);

        dui.add(panel);
    }

    @Override
    public void render(DAL gc) {
        super.render(dal);

        var g = dal.getGraphics();

        if (image != null) {
            g.drawImage(image, gc.getWidth() / 2 - image.getWidth(), gc.getHeight() / 2 - image.getHeight() / 2);
        }
        if (levelImage != null) {
            g.drawImage(levelImage, gc.getWidth() / 2, gc.getHeight() / 2 - levelImage.getHeight() / 2);
        }
        if (intermediate != null) {
            g.drawImage(intermediate, gc.getWidth() / 2 - intermediate.getWidth(),
                    gc.getHeight() / 2 + intermediate.getHeight() / 2);
        }
    }

    @Override
    public void event(DUIEvent event) {
        if (event.getElement() == back) {
            dsh.activate("sdlevelgen", dal);
        } else if (event.getElement() == save) {
            try {
                LevelFileHelper.saveLevel(name, level);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (event.getElement() == generate) {
            if (event.getType() == DButton.MOUSE_UP) {
                try {
                    genLevel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void message(Object o) {
        try {
            var path = (String) o;
            image = new SlickTexture(new Image(path));

            name = new File(path).getName().replace(".png", "");
        } catch (SlickException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void genLevel() throws SlickException {
        // STEP 1: Create "intermediate" image that includes border and thresholds
        // colors
        int width = Integer.parseInt(this.width.getText());
        int height = Integer.parseInt(this.height.getText());

        float tileScaleX = image.getWidth() / (float) width;
        float tileScaleY = image.getHeight() / (float) height;

        int offsetX = 0;
        int offsetY = 0;
        int intermediateWidth = image.getWidth() + (int) (addBorder.checked ? tileScaleX * 2 : 0);
        int intermediateHeight = image.getHeight() + (int) (addBorder.checked ? tileScaleY * 2 : 0);
        if (addBorder.checked) {
            width += 2;
            height += 2;
            offsetX = (int) tileScaleX;
            offsetY = (int) tileScaleY;
        }

        intermediate = new SlickTexture(new Image(intermediateWidth, intermediateHeight));

        // Recalculate tile scale to account for border (though should be the same since
        // we added 2 to width and height)
        tileScaleX = intermediate.getWidth() / (float) width;
        tileScaleY = intermediate.getHeight() / (float) height;

        var tileScaleXFinal = tileScaleX;
        var tileScaleYFinal = tileScaleY;
        var offsetXFinal = offsetX;
        var offsetYFinal = offsetY;

        level = new Level(width, height);
        level.theme = Theme.getTheme("desertrpg");
        cs = new ClientState();
        cs.l = level;
        cs.mgs = new MultiplayerGameScreen();

        intermediate.renderTo(ig -> {
            ig.setAntiAlias(false);
            ig.setColor(DALColor.white);
            ig.fillRect(0, 0, intermediate.getWidth(), intermediate.getHeight());

            if (addBorder.checked) {
                ig.setColor(DALColor.black);
                // TOP
                ig.fillRect(0, 0, intermediate.getWidth(), tileScaleYFinal);
                // BOTTOM
                ig.fillRect(0, intermediate.getHeight() - tileScaleYFinal, intermediate.getWidth(), tileScaleYFinal);
                // LEFT
                ig.fillRect(0, 0, tileScaleXFinal, intermediate.getHeight());
                // RIGHT
                ig.fillRect(intermediate.getWidth() - tileScaleXFinal, 0, tileScaleXFinal, intermediate.getHeight());
            }

            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    DALColor c = image.getColor(x, y);
                    float r = c.r > .5f ? 1.f : 0.f;
                    float g = c.g > .5f ? 1.f : 0.f;
                    float b = c.b > .5f ? 1.f : 0.f;

                    float gray = (r + g + b) / 3.f;

                    if (gray > .4f && gray < .6f) {
                        r = 0.5f;
                        g = 0.5f;
                        b = 0.5f;
                    }

                    ig.setColor(new DALColor(r, g, b));
                    ig.fillRect(x + offsetXFinal, y + offsetYFinal, 1, 1);
                }
            }

            ig.flush();
        });

        // STEP 2: Layout tiles
        int xmax = width;
        switch (mirrorMode.getSelected()) {
            case "X":
                xmax = (int) Math.ceil(width / 2.f);
                break;
        }

        for (int x = 0; x < xmax; x++) {
            int ymax = height;

            switch (mirrorMode.getSelected()) {
                case "Y":
                    ymax = (int) Math.ceil(height / 2.f);
                    break;
                case "XY":
                    ymax = height - (int) ((x / (float) xmax) * height);
                    break;
            }

            for (int y = 0; y < ymax; y++) {
                TileType tt = null;

                ArrayList<Point2i> points = new ArrayList<Point2i>();
                points.add(new Point2i(x, y));

                switch (mirrorMode.getSelected()) {
                    case "X":
                        points.add(new Point2i(width - x - 1, y));
                        break;
                    case "Y":
                        points.add(new Point2i(x, height - y - 1));
                        break;
                    case "XY":
                        points.add(new Point2i(width - x - 1, height - y - 1));
                        break;
                }

                if (tileIsMostlyColor(intermediate, Color.black, x, y, tileScaleX, tileScaleY)) {
                    tt = TileType.WALL;
                } else if (tileIsMostlyColor(image, new Color(.5f, .5f, .5f), x, y, tileScaleX, tileScaleY)) {
                    tt = TileType.GRATE;
                }

                if (tt != null) {
                    for (Point2i p : points) {
                        level.setTile(p.x, p.y, tt);
                    }
                }
            }
        }

        // STEP 3: Layout buildings
        AStarPathFinder finder = new AStarPathFinder(level, 500, StaticFiles.advOptions.getB("diagonalMove"));
        var blobs = findBlobs(intermediate);

        for (var blob : blobs) {
            BuildingType bt = null;

            if (blob.color.equals(Color.green)) {
                bt = BuildingType.POINT;
            } else if (blob.color.equals(Color.red)) {
                bt = BuildingType.CENTER;
            }

            System.out.println(blob.color + " " + bt);

            if (bt != null) {
                int tx = (int) (((blob.pos.x + offsetX) / tileScaleX));
                int ty = (int) (((blob.pos.y + offsetY) / tileScaleY));
                HashSet<Point2i> points = new HashSet<Point2i>();
                switch (mirrorMode.getSelected()) {
                    case "None":
                        points.add(
                                new Point2i((int) ((tx + .5f) * Level.tileSize), (int) ((ty + .5f) * Level.tileSize)));
                        break;
                    case "X":
                        if (tx <= width / 2) {
                            points.add(
                                    new Point2i((int) ((tx + .5f) * Level.tileSize),
                                            (int) ((ty + .5f) * Level.tileSize)));
                            points.add(new Point2i((int) ((width - tx - 1 + .5f) * Level.tileSize),
                                    (int) ((ty + .5f) * Level.tileSize)));
                        }
                        break;
                    case "Y":
                        if (ty <= height / 2) {
                            points.add(
                                    new Point2i((int) ((tx + .5f) * Level.tileSize),
                                            (int) ((ty + .5f) * Level.tileSize)));
                            points.add(new Point2i((int) ((tx + .5f) * Level.tileSize),
                                    (int) ((height - ty - 1 + .5f) * Level.tileSize)));
                        }
                        break;
                    case "XY":
                        // If the blob is in the top left half of the image, split from bottom left to
                        // top right
                        if ((tx / (float) width) + (ty / (float) height) < 1) {
                            points.add(
                                    new Point2i((int) ((tx + .5f) * Level.tileSize),
                                            (int) ((ty + .5f) * Level.tileSize)));
                            points.add(new Point2i((int) ((width - tx - 1 + .5f) * Level.tileSize),
                                    (int) ((height - ty - 1 + .5f) * Level.tileSize)));
                        }
                }

                for (var p : points) {
                    level.buildings.add(new Building(p.x, p.y, bt, null));
                }
            }
        }

        // STEP 4: find two best centers and enforce those
        var centers = level.buildings.stream().filter(b -> b.bt == BuildingType.CENTER)
                .collect(Collectors.toList());
        if (centers.size() >= 2) {
            level.buildings.removeIf(b -> b.bt == BuildingType.CENTER);

            // Find two centers with furthest distance between them
            Building center1 = null;
            Building center2 = null;
            float maxDist = 0;
            for (var c1 : centers) {
                for (var c2 : centers) {
                    if (c1 != c2) {
                        var path = finder.findPath(null, c1.x / Level.tileSize, c1.y / Level.tileSize,
                                c2.x / Level.tileSize, c2.y / Level.tileSize);
                        if (path == null)
                            continue;
                        float dist = path.getLength();
                        if (dist > maxDist) {
                            maxDist = dist;
                            center1 = c1;
                            center2 = c2;
                        }
                    }
                }
            }

            if (center1 != null && center2 != null) {
                center1.t = Team.a;
                center1.hold = Building.HOLDMAX;
                center2.t = Team.b;
                center2.hold = Building.HOLDMAX;
                level.buildings.add(center1);
                level.buildings.add(center2);
            }
        }

        final var centers2 = level.buildings.stream().filter(b -> b.bt == BuildingType.CENTER)
                .collect(Collectors.toList());

        if (centers2.size() == 2) {
            // STEP 5: make sure you can get to both centers from every point, otherwise
            // remove
            level.buildings.removeIf(b -> {
                if (b.bt == BuildingType.CENTER) {
                    return false;
                } else {
                    for (var c : centers2) {
                        var path = finder.findPath(null, c.x / Level.tileSize,
                                c.y / Level.tileSize, b.x / Level.tileSize, b.y / Level.tileSize);
                        if (path == null || path.getLength() < 3) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        GameRenderer gr = new GameRenderer();
        levelImage = gr.renderToTexture(1024, 1024, cs, dal);
    }

    public boolean tileIsMostlyColor(DALTexture im, Color color, int x, int y, float tileScaleX, float tileScaleY) {
        int x1 = (int) (x * tileScaleX);
        int y1 = (int) (y * tileScaleY);
        int x2 = (int) ((x + 1) * tileScaleX);
        int y2 = (int) ((y + 1) * tileScaleY);

        if (x2 >= im.getWidth()) {
            x2 = im.getWidth() - 1;
        }

        if (y2 >= im.getHeight()) {
            y2 = im.getHeight() - 1;
        }

        int colorPixels = 0;
        for (int i = x1; i < x2; i++) {
            for (int j = y1; j < y2; j++) {
                DALColor c = im.getColor(i, j);
                if (c.equals(color)) {
                    colorPixels++;
                }
            }
        }

        int nPixels = (x2 - x1) * (y2 - y1);
        return colorPixels > nPixels / 2;
    }

    public ArrayList<Blob> findBlobs(DALTexture image) {
        ArrayList<Blob> blobs = new ArrayList<Blob>();
        HashSet<Point2i> checked = new HashSet<Point2i>();

        for (int x = 0; x < image.getWidth(); x++) {
            if (x % 10 == 0) {
                System.out.println(x);
            }
            for (int y = 0; y < image.getHeight(); y++) {
                Point2i p = new Point2i(x, y);
                if (checked.contains(p)) {
                    continue;
                }

                DALColor c = image.getColor(x, y);
                if (c.equals(Color.white) || c.equals(Color.black)) {
                    continue;
                }

                int minX = x;
                int maxX = x;
                int minY = y;
                int maxY = y;

                Queue<Point2i> q = new LinkedList<Point2i>();
                q.add(p);
                int count = 0;
                while (!q.isEmpty()) {
                    Point2i p2 = q.poll();
                    if (checked.contains(p2)) {
                        continue;
                    }

                    if (p2.x < 0 || p2.x >= image.getWidth() || p2.y < 0 || p2.y >= image.getHeight()) {
                        continue;
                    }

                    DALColor c2 = image.getColor(p2.x, p2.y);

                    if (!c.equals(c2)) {
                        continue;
                    }

                    checked.add(p2);

                    count++;

                    minX = Math.min(minX, p2.x);
                    maxX = Math.max(maxX, p2.x);
                    minY = Math.min(minY, p2.y);
                    maxY = Math.max(maxY, p2.y);

                    q.add(new Point2i(p2.x - 1, p2.y));
                    q.add(new Point2i(p2.x + 1, p2.y));
                    q.add(new Point2i(p2.x, p2.y - 1));
                    q.add(new Point2i(p2.x, p2.y + 1));
                }

                if (count < 50) {
                    continue;
                }

                blobs.add(new Blob(c, new Point2f((minX + maxX) / 2f, (minY + maxY) / 2f)));
            }
        }

        return blobs;
    }

    public class Blob {
        DALColor color;
        Point2f pos;

        public Blob(DALColor color, Point2f pos) {
            this.color = color;
            this.pos = pos;
        }
    }

    public DUIElement surroundWithLabel(String label, DUIElement e) {
        DRowPanel row = new DRowPanel(0, 0, 0, 0);
        var labelEl = new DText(label, 0, 0);
        labelEl.setSize(300 * uiScale, e.height);

        row.add(labelEl);
        row.add(e);

        return row;
    }
}
