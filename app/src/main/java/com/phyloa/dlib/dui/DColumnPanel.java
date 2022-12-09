package com.phyloa.dlib.dui;

public class DColumnPanel extends DPanel {
    public DColumnPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void calcLayout() {
        int rootY = 0;
        int maxWidth = 0;
        for (var child : children) {
            child.x = 0;
            child.y = rootY;
            rootY += child.height;
            maxWidth = Math.max(maxWidth, child.width);
        }
        width = maxWidth;
        height = rootY;
    }
}
