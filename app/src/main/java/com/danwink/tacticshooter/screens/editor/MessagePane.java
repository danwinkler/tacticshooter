package com.danwink.tacticshooter.screens.editor;

import java.util.ArrayList;

import com.phyloa.dlib.dui.DButton;
import com.phyloa.dlib.dui.DColumnPanel;
import com.phyloa.dlib.dui.DRowPanel;
import com.phyloa.dlib.dui.DScrollPane;
import com.phyloa.dlib.dui.DText;

public class MessagePane extends DScrollPane {
    DColumnPanel rows = new DColumnPanel();

    public MessagePane(int x, int y, int width, int height) {
        super(x, y, width, height);

        add(rows);
    }

    public void addMessage(String message) {
        int rowHeight = 20;
        DRowPanel p = new DRowPanel();
        p.setDrawBackground(true);
        var text = new DText(message, 0, 0, false);
        text.width = width - 20 - 10;
        text.height = rowHeight;
        p.add(text);

        var button = new DButton("X", 0, 0, 20, rowHeight);
        button.onMouseUp(e -> {
            rows.remove(p);
            ui.doLayout();
            setInnerPaneHeight(rows.height);
        });
        p.add(button);

        rows.add(p);
        ui.doLayout();
        setInnerPaneHeight(rows.height);
    }
}
