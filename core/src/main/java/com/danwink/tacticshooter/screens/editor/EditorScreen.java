package com.danwink.tacticshooter.screens.editor;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.newdawn.slick.Input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.danwink.tacticshooter.KryoHelper;
import com.danwink.tacticshooter.LevelFileHelper;
import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.Theme;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.DAL.DALGraphics;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.danwink.tacticshooter.gameobjects.Team;
import com.danwink.tacticshooter.renderer.BuildingRenderer;
import com.danwink.tacticshooter.renderer.FloorRenderer;
import com.danwink.tacticshooter.renderer.WallRenderer;
import com.danwink.tacticshooter.slick.Slick2DCamera;
import com.danwink.tacticshooter.ui.DUIScreen;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DKeyEvent;
import com.phyloa.dlib.dui.DMouseEvent;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.RelativePosition;
import com.phyloa.dlib.math.Point2i;
import com.phyloa.dlib.renderer.Renderer2D;

public class EditorScreen extends DUIScreen {
    LevelElement levelElement;
    MessagePane messagePane;
    FilePane filePane;

    @Override
    public void init(DAL dal) {
        this.clearScreen = true;

        levelElement = new LevelElement();
        messagePane = new MessagePane(0, 0, 0, 0);
    }

    @Override
    public void createUIElements(DUI dui, float windowHeight) {
        levelElement.setSize(dui.rootPane.width, dui.rootPane.height);

        var toolPane = new ToolPane(levelElement, uiScale);
        toolPane.setRelativePosition(RelativePosition.TOP_RIGHT, 0, 0);
        dui.add(toolPane);

        filePane = new FilePane(levelElement, uiScale, dui);
        filePane.setRelativePosition(RelativePosition.TOP_LEFT, 0, 0);
        dui.add(filePane);

        var exitButton = new DButton("Exit", 0, 0, 150 * uiScale, 50 * uiScale);
        exitButton.setRelativePosition(RelativePosition.BOTTOM_LEFT, 0, 0);
        exitButton.onMouseUp(e -> {
            dsh.activate("home", dal, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
        });
        dui.add(exitButton);

        messagePane.setRelativePosition(RelativePosition.BOTTOM_RIGHT, 0, 0);
        messagePane.setSize(400 * uiScale, 160 * uiScale);
        messagePane.renderBackground = false;
        dui.add(messagePane);

        dui.add(levelElement);
    }

    @Override
    public void event(DUIEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void message(Object o) {
        // TODO Auto-generated method stub

    }

    public class LevelElement extends DUIElement {
        String mapName;
        Level level;

        Slick2DCamera camera;
        public FloorRenderer floor;
        public WallRenderer wall;
        public BuildingRenderer building;

        PlaceType placeType = PlaceType.WALL;
        MirrorType mirrorType = MirrorType.NONE;
        BrushType brushType = BrushType.POINT;

        int lastMouseX, lastMouseY;

        Kryo kryo;
        LinkedList<byte[]> undoStack = new LinkedList<byte[]>();

        public LevelElement() {
            camera = new Slick2DCamera();
            floor = new FloorRenderer();
            wall = new WallRenderer();
            building = new BuildingRenderer();

            kryo = new Kryo();
            KryoHelper.register(kryo);
        }

        public void setLevel(Level level) {
            this.level = level;

            level.theme = Theme.getTheme("desertrpg");

            camera.x = level.width * Level.tileSize / 2;
            camera.y = level.height * Level.tileSize / 2;

            // Reset textures if they are already present
            wall.texture = null;
            floor.texture = null;

            undoStack.clear();
            pushUndoState();
        }

        public void newLevel(int width, int height) {
            var l = new Level(width, height);
            setLevel(l);
            mapName = "";
        }

        @Override
        public void render(Renderer2D<DALTexture> r) {
            if (level == null) {
                return;
            }

            var g = r.getGraphics();

            g.setAntiAlias(false);

            camera.start(dal.getGraphics());

            floor.render(dal, level);
            wall.render(dal, level);
            building.render(dal.getGraphics(), level, false);

            mirrorType.mirror.getPoints(level, x, y).stream()
                    .flatMap(p -> brushType.brush.getPoints(level, p.x, p.y).stream())
                    .forEach(p -> placeType.placer.render(g));

            g.setColor(DALColor.black);
            for (var b : level.buildings) {
                var str = b.bt.name();
                if (str != null) {
                    var ts = g.getStringSize(str);
                    var tw = ts.x;
                    var th = ts.y;
                    g.drawText(str, b.x - tw / 2, b.y - th / 2);
                }
            }

            camera.end(dal.getGraphics());
        }

        public void pushUndoState() {
            Output output = new Output(1024, -1);
            kryo.writeObject(output, this.level);
            undoStack.push(output.toBytes());
        }

        public void undo() {
            if (undoStack.size() > 1) {
                undoStack.pop();
                var input = new com.esotericsoftware.kryo.io.Input(undoStack.peek());
                var l = kryo.readObject(input, Level.class);
                l.theme = level.theme;
                level = l;

                floor.redrawLevel(level);
                wall.redrawLevel(level);
            }
        }

        @Override
        public void update(DUI ui) {
            // TODO Auto-generated method stub

        }

        @Override
        public void keyPressed(DKeyEvent dke) {
            switch (dke.keyCode) {
                case KeyEvent.VK_Z:
                    // if (level != null && input.isKeyDown(Input.KEY_LCONTROL)) {
                    if (level != null && Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
                        undo();
                    }
                    break;
                case KeyEvent.VK_S:
                    // if (level != null && input.isKeyDown(Input.KEY_LCONTROL)) {
                    if (level != null && Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
                        if (mapName != null && !mapName.isEmpty()) {
                            try {
                                LevelFileHelper.saveLevel(mapName, level);
                                messagePane.addMessage("Saved as " + mapName);
                            } catch (IOException e) {
                                e.printStackTrace();
                                messagePane.addMessage("Error saving map: " + e.getMessage());
                            }
                        } else {
                            filePane.openSaveMenu();
                        }
                    }
                    break;
            }
        }

        @Override
        public void keyReleased(DKeyEvent dke) {
            // TODO Auto-generated method stub

        }

        @Override
        public void keyTyped(DKeyEvent dke) {

        }

        @Override
        public void mouseEntered(DMouseEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public void mouseExited(DMouseEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean mousePressed(DMouseEvent e) {
            if (level == null) {
                return false;
            }

            var world = camera.screenToWorld(e.x, e.y, dal.getGraphics());
            int tx = (int) world.x / Level.tileSize;
            int ty = (int) world.y / Level.tileSize;

            // if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
            if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
                if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
                    mirrorType.mirror.getPoints(level, tx, ty).stream()
                            .flatMap(p -> brushType.brush.getPoints(level, p.x, p.y).stream())
                            .forEach(p -> placeType.placer.mouseDown(this, p.x, p.y));
                }
            }

            lastMouseX = e.x;
            lastMouseY = e.y;
            return false;
        }

        @Override
        public boolean mouseReleased(DMouseEvent e) {
            if (level == null) {
                return false;
            }

            var world = camera.screenToWorld(e.x, e.y, dal.getGraphics());
            int tx = (int) world.x / Level.tileSize;
            int ty = (int) world.y / Level.tileSize;

            if (e.button == Input.MOUSE_LEFT_BUTTON) {
                if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
                    mirrorType.mirror.getPoints(level, tx, ty).stream()
                            .flatMap(p -> brushType.brush.getPoints(level, p.x, p.y).stream())
                            .forEach(p -> placeType.placer.mouseUp(this, p.x, p.y));

                    pushUndoState();
                }
            }

            return true;
        }

        @Override
        public void mouseMoved(DMouseEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean mouseDragged(DMouseEvent e) {
            if (level == null) {
                return false;
            }

            var world = camera.screenToWorld(e.x, e.y, dal.getGraphics());
            int tx = (int) world.x / Level.tileSize;
            int ty = (int) world.y / Level.tileSize;

            // if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
            if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
                if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
                    mirrorType.mirror.getPoints(level, tx, ty).stream()
                            .flatMap(p -> brushType.brush.getPoints(level, p.x, p.y).stream())
                            .forEach(p -> placeType.placer.mouseDragged(this, p.x, p.y));
                }
                // } else if (input.isMouseButtonDown(Input.MOUSE_MIDDLE_BUTTON)) {
            } else if (Gdx.input.isButtonPressed(Buttons.MIDDLE)) {
                camera.x += (lastMouseX - e.x) / camera.zoom;
                camera.y += (lastMouseY - e.y) / camera.zoom;
            }
            lastMouseX = e.x;
            lastMouseY = e.y;

            return true;
        }

        @Override
        public void mouseWheel(DMouseEvent e) {
            if (level == null) {
                return;
            }

            // This is a hack because DUI currently doesn't let mouseWheel return whether or
            // not the event was consumed
            if (messagePane.isInside) {
                return;
            }

            float zoomSpeed = 0.1f;
            int dir = e.wheel > 0 ? 1 : -1;
            float zoom = dir * zoomSpeed;

            var mx = Gdx.input.getX();
            var my = Gdx.input.getY();

            var worldCoords = camera.screenToWorld(mx, my, dal.getGraphics());

            camera.zoom(zoom);

            var afterWorldCoords = camera.screenToWorld(mx, my, dal.getGraphics());

            camera.x += worldCoords.x - afterWorldCoords.x;
            camera.y += worldCoords.y - afterWorldCoords.y;
        }
    }

    @Override
    public void onResize(int width, int height) {
        super.onResize(width, height);

        levelElement.setSize(width, height);
    }

    public interface Placer {
        public void mouseDown(LevelElement l, int x, int y);

        public default void mouseUp(LevelElement l, int x, int y) {
        };

        public default void mouseDragged(LevelElement l, int x, int y) {

        };

        public default void render(DALGraphics g) {

        }
    }

    public static class TilePlacer implements Placer {
        private TileType tile;

        public HashSet<Point2i> points = new HashSet<>();

        public TilePlacer(TileType tile) {
            this.tile = tile;
        }

        @Override
        public void mouseDown(LevelElement l, int x, int y) {
            points.add(new Point2i(x, y));
        }

        @Override
        public void mouseUp(LevelElement l, int x, int y) {
            points.forEach(p -> l.level.setTile(p.x, p.y, tile));
            points.clear();

            l.floor.redrawLevel(l.level);
            l.wall.redrawLevel(l.level);
        }

        @Override
        public void mouseDragged(LevelElement l, int x, int y) {
            points.add(new Point2i(x, y));
        }

        @Override
        public void render(DALGraphics g) {
            points.forEach(p -> {
                g.setColor(DALColor.red);
                g.drawRect(p.x * Level.tileSize, p.y * Level.tileSize, Level.tileSize, Level.tileSize);
            });
        }
    }

    public static class BuildingPlacer implements Placer {
        private BuildingType building;

        public BuildingPlacer(BuildingType building) {
            this.building = building;
        }

        @Override
        public void mouseDown(LevelElement l, int x, int y) {
            int wx = x * Level.tileSize + Level.tileSize / 2;
            int wy = y * Level.tileSize + Level.tileSize / 2;

            for (Building b : l.level.buildings) {
                if (b.x == wx && b.y == wy) {
                    return;
                }
            }

            Building b = new Building(wx, wy, building, null);
            l.level.buildings.add(b);
        }
    }

    public static class EraserPlacer extends TilePlacer {
        public EraserPlacer() {
            super(TileType.FLOOR);
        }

        @Override
        public void mouseDown(LevelElement l, int x, int y) {
            super.mouseDown(l, x, y);

            eraseBuildings(l, x, y);
        }

        @Override
        public void mouseUp(LevelElement l, int x, int y) {
            super.mouseUp(l, x, y);
        }

        @Override
        public void mouseDragged(LevelElement l, int x, int y) {
            super.mouseDragged(l, x, y);

            eraseBuildings(l, x, y);
        }

        public void eraseBuildings(LevelElement l, int x, int y) {
            for (int i = 0; i < l.level.buildings.size(); i++) {
                Building b = l.level.buildings.get(i);
                if (b.x / Level.tileSize == x && b.y / Level.tileSize == y) {
                    l.level.buildings.remove(i);
                    i--;
                }
            }
        }
    }

    public interface Brush {
        public List<Point2i> getPoints(Level l, int x, int y);
    }

    public interface Mirror {
        public List<Point2i> getPoints(Level l, int x, int y);
    }

    public enum PlaceType {
        FLOOR(new TilePlacer(TileType.FLOOR)),
        WALL(new TilePlacer(TileType.WALL)),
        GRATE(new TilePlacer(TileType.GRATE)),
        CENTER(new BuildingPlacer(BuildingType.CENTER)),
        POINT(new BuildingPlacer(BuildingType.POINT)),
        TOGGLETEAM((l, x, y) -> {
            for (int i = 0; i < l.level.buildings.size(); i++) {
                Building b = l.level.buildings.get(i);
                if (b.x / Level.tileSize == x && b.y / Level.tileSize == y) {
                    if (b.t == Team.a) {
                        b.t = Team.b;
                        b.hold = Building.HOLDMAX;
                    } else if (b.t == Team.b) {
                        b.t = null;
                        b.hold = 0;
                    } else {
                        b.t = Team.a;
                        b.hold = Building.HOLDMAX;
                    }
                }
            }
        }),

        ERASER(new EraserPlacer());

        Placer placer;

        PlaceType(Placer placer) {
            this.placer = placer;
        }
    }

    public enum BrushType {
        POINT((l, x, y) -> {
            List<Point2i> points = new ArrayList<>();
            points.add(new Point2i(x, y));
            return points;
        });

        Brush brush;

        BrushType(Brush brush) {
            this.brush = brush;
        }
    }

    public enum MirrorType {
        NONE((l, x, y) -> {
            List<Point2i> points = new ArrayList<>();
            points.add(new Point2i(x, y));
            return points;
        }),

        X((l, x, y) -> {
            List<Point2i> points = new ArrayList<>();
            points.add(new Point2i(x, y));
            points.add(new Point2i(l.width - x - 1, y));
            return points;
        }),
        Y((l, x, y) -> {
            List<Point2i> points = new ArrayList<>();
            points.add(new Point2i(x, y));
            points.add(new Point2i(x, l.height - y - 1));
            return points;
        }),
        XY((l, x, y) -> {
            List<Point2i> points = new ArrayList<>();
            points.add(new Point2i(x, y));
            points.add(new Point2i(l.width - x - 1, l.height - y - 1));
            return points;
        }),
        FOURWAY((l, x, y) -> {
            List<Point2i> points = new ArrayList<>();
            points.add(new Point2i(x, y));
            points.add(new Point2i(l.width - x - 1, y));
            points.add(new Point2i(x, l.height - y - 1));
            points.add(new Point2i(l.width - x - 1, l.height - y - 1));
            return points;
        });

        Mirror mirror;

        MirrorType(Mirror mirror) {
            this.mirror = mirror;
        }
    }
}
