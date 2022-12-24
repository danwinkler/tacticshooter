package com.danwink.tacticshooter.screens.editor;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.Theme;
import com.danwink.tacticshooter.gameobjects.Building;
import com.danwink.tacticshooter.gameobjects.Level;
import com.danwink.tacticshooter.gameobjects.Team;
import com.danwink.tacticshooter.gameobjects.Building.BuildingType;
import com.danwink.tacticshooter.gameobjects.Level.TileType;
import com.danwink.tacticshooter.renderer.BuildingRenderer;
import com.danwink.tacticshooter.renderer.FloorRenderer;
import com.danwink.tacticshooter.renderer.WallRenderer;
import com.danwink.tacticshooter.slick.Slick2DCamera;
import com.danwink.tacticshooter.ui.DUIScreen;
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

    Input input;

    @Override
    public void init(GameContainer gc) {
        input = gc.getInput();
        levelElement = new LevelElement();
    }

    @Override
    public void createUIElements(DUI dui, float windowHeight) {
        levelElement.setSize(dui.rootPane.width, dui.rootPane.height);

        var toolPane = new ToolPane(levelElement, uiScale);
        toolPane.setRelativePosition(RelativePosition.TOP_RIGHT, 0, 0);
        dui.add(toolPane);

        var filePane = new FilePane(levelElement, uiScale, dui);
        filePane.setRelativePosition(RelativePosition.TOP_LEFT, 0, 0);
        dui.add(filePane);

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
        Level level;

        Slick2DCamera camera;
        public FloorRenderer floor;
        public WallRenderer wall;
        public BuildingRenderer building;

        PlaceType placeType = PlaceType.WALL;
        MirrorType mirrorType = MirrorType.NONE;
        BrushType brushType = BrushType.POINT;

        int lastMouseX, lastMouseY;

        public LevelElement() {
            camera = new Slick2DCamera();
            floor = new FloorRenderer();
            wall = new WallRenderer();
            building = new BuildingRenderer();
        }

        public void setLevel(Level level) {
            this.level = level;

            camera.x = level.width * Level.tileSize / 2;
            camera.y = level.height * Level.tileSize / 2;
        }

        public void newLevel(int width, int height) {
            var l = new Level(width, height);
            try {
                l.theme = Theme.getTheme("desertrpg");
            } catch (SlickException e) {
                e.printStackTrace();
            }
            setLevel(l);
        }

        @Override
        public void render(Renderer2D<Image> r) {
            if (level == null) {
                return;
            }

            var g = r.getRenderer();
            g.setAntiAlias(false);

            camera.start(gc, g);

            floor.render(g, level);
            wall.render(g, level);
            building.render(g, level, false);

            for (var b : level.buildings) {
                var str = b.bt.name();
                if (str != null) {
                    var tw = g.getFont().getWidth(str);
                    var th = g.getFont().getHeight(str);
                    g.drawString(str, b.x - tw / 2, b.y - th / 2);
                }
            }

            camera.end(g);
        }

        @Override
        public void update(DUI ui) {
            // TODO Auto-generated method stub

        }

        public void place(int x, int y) {
            if (level == null) {
                return;
            }

            mirrorType.mirror.getPoints(level, x, y).stream()
                    .flatMap(p -> brushType.brush.getPoints(level, p.x, p.y).stream())
                    .forEach(p -> placeType.placer.place(level, p.x, p.y));

            floor.redrawLevel(level);
            wall.redrawLevel(level);
        }

        @Override
        public void keyPressed(DKeyEvent dke) {

        }

        @Override
        public void keyReleased(DKeyEvent dke) {
            // TODO Auto-generated method stub

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

            var world = camera.screenToWorld(e.x, e.y, gc);
            int tx = (int) world.x / Level.tileSize;
            int ty = (int) world.y / Level.tileSize;

            if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
                if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
                    place(tx, ty);
                }
            }

            lastMouseX = e.x;
            lastMouseY = e.y;
            return false;
        }

        @Override
        public boolean mouseReleased(DMouseEvent e) {
            // TODO Auto-generated method stub
            return false;
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

            var world = camera.screenToWorld(e.x, e.y, gc);
            int tx = (int) world.x / Level.tileSize;
            int ty = (int) world.y / Level.tileSize;

            if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
                if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
                    place(tx, ty);
                }
            } else if (input.isMouseButtonDown(Input.MOUSE_MIDDLE_BUTTON)) {
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

            float zoomSpeed = 0.1f;
            int dir = e.wheel > 0 ? 1 : -1;
            float zoom = dir * zoomSpeed;

            var mx = input.getMouseX();
            var my = input.getMouseY();

            var worldCoords = camera.screenToWorld(mx, my, gc);

            camera.zoom(zoom);

            var afterWorldCoords = camera.screenToWorld(mx, my, gc);

            camera.x += worldCoords.x - afterWorldCoords.x;
            camera.y += worldCoords.y - afterWorldCoords.y;
        }
    }

    public interface Placer {
        public void place(Level l, int x, int y);
    }

    public interface Brush {
        public List<Point2i> getPoints(Level l, int x, int y);
    }

    public interface Mirror {
        public List<Point2i> getPoints(Level l, int x, int y);
    }

    public enum PlaceType {
        FLOOR((l, x, y) -> l.setTile(x, y, TileType.FLOOR)),
        WALL((l, x, y) -> l.setTile(x, y, TileType.WALL)),
        GRATE((l, x, y) -> l.setTile(x, y, TileType.GRATE)),
        CENTER((l, x, y) -> {
            Building b = new Building(x * Level.tileSize + Level.tileSize / 2,
                    y * Level.tileSize + Level.tileSize / 2, BuildingType.CENTER, null);
            l.buildings.add(b);
        }),
        POINT((l, x, y) -> {
            Building b = new Building(x * Level.tileSize + Level.tileSize / 2,
                    y * Level.tileSize + Level.tileSize / 2, BuildingType.POINT, null);
            l.buildings.add(b);
        }),
        TOGGLETEAM((l, x, y) -> {
            for (int i = 0; i < l.buildings.size(); i++) {
                Building b = l.buildings.get(i);
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
        ERASER((l, x, y) -> {
            l.setTile(x, y, TileType.FLOOR);
            for (int i = 0; i < l.buildings.size(); i++) {
                Building b = l.buildings.get(i);
                if (b.x / Level.tileSize == x && b.y / Level.tileSize == y) {
                    l.buildings.remove(i);
                    i--;
                }
            }
        });

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
