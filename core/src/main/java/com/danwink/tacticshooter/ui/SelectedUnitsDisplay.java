package com.danwink.tacticshooter.ui;

import java.awt.Color;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.danwink.tacticshooter.ClientState;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.dal.DAL.DALTexture;
import com.danwink.tacticshooter.gameobjects.Unit;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DKeyEvent;
import com.phyloa.dlib.dui.DMouseEvent;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.RelativePosition;
import com.phyloa.dlib.renderer.Renderer2D;

public class SelectedUnitsDisplay extends DUIElement {
    int maxCols;
    int portraitSize;
    int margin = 4;

    public ClientState cs;

    public SelectedUnitsDisplay(RelativePosition relative, int maxCols, int portraitSize) {
        super(relative, 0, 0, 0, 0);
        this.maxCols = maxCols;
        this.portraitSize = portraitSize;
    }

    public void setClientState(ClientState cs) {
        this.cs = cs;
    }

    public void setPortraitSize(int size) {
        this.portraitSize = size;
        this.margin = portraitSize / 12;
    }

    @Override
    public void calcLayout(DUI dui) {
        this.clearChildren();

        if (cs == null || cs.selected == null || cs.selected.size() == 0)
            return;

        int cols = Math.min(cs.selected.size(), maxCols);
        int rows = (int) Math.ceil(cs.selected.size() / (float) maxCols);

        width = cols * portraitSize + (cols - 1) * margin;
        height = rows * portraitSize + (rows - 1) * margin;

        for (int i = 0; i < cs.selected.size(); i++) {
            int col = i % maxCols;
            int row = i / maxCols;

            int x = col * (portraitSize + margin);
            int y = row * (portraitSize + margin);

            var portraitButton = new PortraitButton(cs.unitMap.get(cs.selected.get(i)), portraitSize);
            portraitButton.setLocation(x, y);
            this.add(portraitButton);
        }
    }

    @Override
    public void keyPressed(DKeyEvent dke) {
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub
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
        return true;
    }

    @Override
    public void mouseWheel(DMouseEvent dme) {
        // TODO Auto-generated method stub

    }

    @Override
    public void render(Renderer2D<DALTexture> r) {
        // TODO Auto-generated method stub

    }

    @Override
    public void update(DUI ui) {
        // TODO Auto-generated method stub

    }

    public class PortraitButton extends DButton {
        private Unit u;

        public PortraitButton(Unit u, int size) {
            super("", 0, 0, size, size);

            this.u = u;
        }

        @Override
        public void render(Renderer2D<DALTexture> r) {
            r.pushMatrix();
            r.translate(x, y);
            var portraitImage = cs.l.theme.getPortrait(u.type.name);
            r.drawImage(portraitImage, 0, 0, width, height, DALColor.white);

            var healthBarWidth = width * (u.health / (float) u.type.health);
            int healthBarHeight = height / 12;
            r.color(Color.GREEN);
            r.fillRect(0, height - healthBarHeight, healthBarWidth, healthBarHeight);
            r.color(Color.BLACK);
            r.drawRect(0, height - healthBarHeight, healthBarWidth, healthBarHeight);
            r.popMatrix();
        }

        @Override
        public boolean mousePressed(DMouseEvent e) {
            if (isInside(e.x, e.y)) {
                state = PRESSED;
                ui.setFocus(this);

                if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
                    ArrayList<Integer> toKeep = new ArrayList<Integer>();
                    for (int i = 0; i < cs.selected.size(); i++) {
                        var ou = cs.unitMap.get(cs.selected.get(i));
                        if (ou.type.name.equals(u.type.name)) {
                            toKeep.add(cs.selected.get(i));
                        } else {
                            ou.selected = false;
                        }
                    }

                    cs.selected.clear();
                    cs.selected.addAll(toKeep);
                } else {
                    cs.clearSelected();
                    cs.selected.add(u.id);
                    u.selected = true;
                }
                ui.doLayout();

                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(DMouseEvent e) {
            if (isInside(e.x, e.y)) {
                state = HOVER;
                ui.setFocus(this);
                return true;
            } else {
                state = RELEASED;
            }
            return false;
        }
    }
}
