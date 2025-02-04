package com.danwink.tacticshooter.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.danwink.tacticshooter.UIHelper;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.phyloa.dlib.dui.DEventMapper;
import com.phyloa.dlib.dui.DKeyEvent;
import com.phyloa.dlib.dui.DKeyListener;
import com.phyloa.dlib.dui.DMouseEvent;
import com.phyloa.dlib.dui.DMouseListener;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;

/**
 * A superclass for screens to DRY out some common DUI logic (like scaling)
 */
public abstract class DUIScreen extends DScreen<DAL> implements DUIListener {
    public DUI dui;
    public DAL dal;

    public int uiScale;

    public boolean clearScreen = false;

    public abstract void init(DAL dal);

    public abstract void createUIElements(DUI dui, float windowHeight);

    public void onActivate(DAL dal, DScreenHandler<DAL> dsh) {
        this.dal = dal;

        dui = new DUI(dal.getEventMapper(), 0, 0, dal.getWidth(), dal.getHeight());
        uiScale = UIHelper.getUIScale(dal.getHeight());
        init(dal);
        createUIElements(dui, dal.getHeight());
        dui.doLayout();
        dui.setEnabled(true);
        dui.addDUIListener(this);
    }

    public void update(DAL dal, float delta) {
        dui.update();
    }

    public void render(DAL dal) {
        var g = dal.getGraphics();

        if (clearScreen) {
            g.clear();
            g.setColor(DALColor.lightGray);
            g.fillRect(0, 0, dal.getWidth(), dal.getHeight());
        }

        dui.render(DAL.getDUIRenderer(g));
    }

    public void onExit() {
        dui.setEnabled(false);
        dui = null;
    }

    public void onResize(int width, int height) {
        dui.resize(width, height);
        int nextUIScale = UIHelper.getUIScale(height);
        if (nextUIScale != uiScale) {
            uiScale = nextUIScale;
            dui.rootPane.clearChildren();
            createUIElements(dui, height);
        }
        dui.doLayout();
    }

    public static class Gdx2DEventMapper implements DEventMapper, InputProcessor {
        public static Set<Integer> actionKeys = new HashSet<>(
                Arrays.asList(
                        Keys.ENTER,
                        Keys.ESCAPE,
                        Keys.UP,
                        Keys.DOWN,
                        Keys.LEFT,
                        Keys.RIGHT,
                        Keys.ALT_LEFT,
                        Keys.ALT_RIGHT,
                        Keys.CONTROL_LEFT,
                        Keys.CONTROL_RIGHT,
                        Keys.SHIFT_LEFT,
                        Keys.SHIFT_RIGHT,
                        Keys.TAB));

        ArrayList<DKeyListener> keyListeners = new ArrayList<DKeyListener>();
        ArrayList<DMouseListener> mouseListeners = new ArrayList<DMouseListener>();

        boolean enabled = true;

        @Override
        public void addDKeyListener(DKeyListener l) {
            if (!keyListeners.contains(l)) {
                keyListeners.add(l);
            }
        }

        @Override
        public void addDMouseListener(DMouseListener l) {
            if (!mouseListeners.contains(l)) {
                mouseListeners.add(l);
            }
        }

        @Override
        public void removeDKeyListener(DKeyListener l) {
            keyListeners.remove(l);
        }

        @Override
        public void removeDMouseListener(DMouseListener l) {
            mouseListeners.remove(l);
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (!enabled) {
                return false;
            }

            DKeyEvent e = new DKeyEvent();
            e.keyCode = keycode;
            e.lctrl = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT);
            e.keyChar = Keys.toString(keycode).charAt(0);
            e.isActionKey = actionKeys.contains(keycode);

            for (var l : keyListeners) {
                l.keyPressed(e);
            }

            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            if (!enabled) {
                return false;
            }

            DKeyEvent e = new DKeyEvent();
            e.keyCode = keycode;
            e.lctrl = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT);

            for (var l : keyListeners) {
                l.keyReleased(e);
            }

            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            if (!enabled) {
                return false;
            }

            DKeyEvent e = new DKeyEvent();
            e.keyChar = character;

            System.out.println("Key typed: " + e.keyChar);

            for (var l : keyListeners) {
                l.keyTyped(e);
            }

            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (!enabled) {
                return false;
            }

            DMouseEvent e = new DMouseEvent();

            e.x = screenX;
            e.y = screenY;
            e.button = button;

            for (var l : mouseListeners) {
                l.mousePressed(e);
            }

            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (!enabled) {
                return false;
            }

            DMouseEvent e = new DMouseEvent();

            e.x = screenX;
            e.y = screenY;
            e.button = button;

            for (var l : mouseListeners) {
                l.mouseReleased(e);
            }

            return false;
        }

        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (!enabled) {
                return false;
            }

            DMouseEvent e = new DMouseEvent();

            e.x = screenX;
            e.y = screenY;
            // e.button = pointer;

            for (var l : mouseListeners) {
                l.mouseDragged(e);
            }

            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            if (!enabled) {
                return false;
            }

            DMouseEvent e = new DMouseEvent();

            e.x = screenX;
            e.y = screenY;

            for (var l : mouseListeners) {
                l.mouseMoved(e);
            }

            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            if (!enabled) {
                return false;
            }

            DMouseEvent e = new DMouseEvent();

            e.wheel = (int) -amountY;
            e.x = Gdx.input.getX();
            e.y = Gdx.input.getY();

            for (var l : mouseListeners) {
                l.mouseWheel(e);
            }

            return false;
        }
    }
}
