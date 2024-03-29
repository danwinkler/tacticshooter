package com.danwink.tacticshooter.ui;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import com.danwink.tacticshooter.UIHelper;
import com.danwink.tacticshooter.slick.Slick2DEventMapper;
import com.danwink.tacticshooter.slick.Slick2DRenderer;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIListener;
import com.phyloa.dlib.game.DScreen;
import com.phyloa.dlib.game.DScreenHandler;

/**
 * A superclass for screens to DRY out some common DUI logic (like scaling)
 */
public abstract class DUIScreen extends DScreen<GameContainer, Graphics> implements DUIListener {
    public DUI dui;

    Slick2DRenderer r = new Slick2DRenderer();

    public int uiScale;

    public abstract void init(GameContainer gc);

    public abstract void createUIElements(DUI dui, float windowHeight);

    public void onActivate(GameContainer e, DScreenHandler<GameContainer, Graphics> dsh) {
        dui = new DUI(new Slick2DEventMapper(e.getInput()), 0, 0, gc.getWidth(), gc.getHeight());
        uiScale = UIHelper.getUIScale(gc.getHeight());
        init(e);
        createUIElements(dui, gc.getHeight());
        dui.doLayout();
        dui.setEnabled(true);
        dui.addDUIListener(this);
    }

    public void update(GameContainer gc, float delta) {
        dui.update();
    }

    public void render(GameContainer gc, Graphics g) {
        dui.render(r.renderTo(g));
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
}
