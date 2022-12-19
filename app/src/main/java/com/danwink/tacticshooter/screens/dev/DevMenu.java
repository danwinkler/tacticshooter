package com.danwink.tacticshooter.screens.dev;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.danwink.tacticshooter.StaticFiles;
import com.danwink.tacticshooter.ui.DUIScreen;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DColumnPanel;
import com.phyloa.dlib.dui.DUI;
import com.phyloa.dlib.dui.DUIElement;
import com.phyloa.dlib.dui.DUIEvent;
import com.phyloa.dlib.dui.RelativePosition;

public class DevMenu extends DUIScreen {
    DButton spriteEditor;
    DButton sdLevelGen;
    DButton back;

    @Override
    public void init(GameContainer gc) {
        // TODO Auto-generated method stub

    }

    @Override
    public void createUIElements(DUI dui, float windowHeight) {
        DColumnPanel buttonpanel = new DColumnPanel(0, 0, 0, 0);
        buttonpanel.setRelativePosition(RelativePosition.CENTER, 0, 0);

        spriteEditor = new DButton("Sprite Editor", 0, 0, 200 * uiScale, 100 * uiScale);
        buttonpanel.add(spriteEditor);

        sdLevelGen = new DButton("SD Level Gen", 0, 0, 200 * uiScale, 100 * uiScale);
        buttonpanel.add(sdLevelGen);

        back = new DButton("Back", 0, 0, 200 * uiScale, 100 * uiScale);
        buttonpanel.add(back);

        dui.add(buttonpanel);
    }

    @Override
    public void event(DUIEvent event) {
        DUIElement e = event.getElement();
        if (e instanceof DButton && event.getType() == DButton.MOUSE_UP) {
            if (e == back) {
                dsh.activate("home", gc, StaticFiles.getUpMenuOut(), StaticFiles.getUpMenuIn());
            } else if (e == spriteEditor) {
                dsh.activate("spriteeditor", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn());
            } else if (e == sdLevelGen) {
                dsh.activate("sdlevelgen", gc, StaticFiles.getDownMenuOut(), StaticFiles.getDownMenuIn());
            }
        }
    }

    @Override
    public void message(Object o) {
        // TODO Auto-generated method stub

    }

}
