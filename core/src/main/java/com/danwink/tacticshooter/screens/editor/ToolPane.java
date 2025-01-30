package com.danwink.tacticshooter.screens.editor;

import com.danwink.tacticshooter.screens.editor.EditorScreen.BrushType;
import com.danwink.tacticshooter.screens.editor.EditorScreen.LevelElement;
import com.danwink.tacticshooter.screens.editor.EditorScreen.MirrorType;
import com.danwink.tacticshooter.screens.editor.EditorScreen.PlaceType;
import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DColumnPanel;
import com.phyloa.dlib.dui.DSpacer;

public class ToolPane extends DColumnPanel {
    public ToolPane(LevelElement levelElement, int uiScale) {
        int buttonWidth = 150 * uiScale;
        int buttonHeight = 50 * uiScale;
        for (var place : PlaceType.values()) {
            var button = new DButton(place.name(), 0, 0, buttonWidth, buttonHeight);
            button.onMouseUp(e -> levelElement.placeType = place);
            add(button);
        }
        add(new DSpacer(0, buttonHeight / 2));
        for (var brush : BrushType.values()) {
            var button = new DButton(brush.name(), 0, 0, buttonWidth, buttonHeight);
            button.onMouseUp(e -> levelElement.brushType = brush);
            add(button);
        }
        add(new DSpacer(0, buttonHeight / 2));
        for (var mirror : MirrorType.values()) {
            var button = new DButton(mirror.name(), 0, 0, buttonWidth, buttonHeight);
            button.onMouseUp(e -> levelElement.mirrorType = mirror);
            add(button);
        }
    }
}
