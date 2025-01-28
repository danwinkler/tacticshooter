package com.danwink.tacticshooter.ui;

import com.danwink.tacticshooter.UIHelper;
import com.danwink.tacticshooter.dal.DAL;
import com.danwink.tacticshooter.dal.SlickDAL;
import com.danwink.tacticshooter.dal.DAL.DALColor;
import com.danwink.tacticshooter.slick.Slick2DEventMapper;
import com.danwink.tacticshooter.slick.Slick2DRenderer;
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

    Slick2DRenderer r = new Slick2DRenderer();

    public int uiScale;

    public boolean clearScreen = false;

    public abstract void init(DAL dal);

    public abstract void createUIElements(DUI dui, float windowHeight);

    public void onActivate(DAL dal, DScreenHandler<DAL> dsh) {
        this.dal = dal;

        var gc = ((SlickDAL) dal).gc;

        dui = new DUI(new Slick2DEventMapper(gc.getInput()), 0, 0, dal.getWidth(), dal.getHeight());
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
}
