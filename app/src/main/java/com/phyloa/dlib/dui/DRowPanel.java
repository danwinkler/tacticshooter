package com.phyloa.dlib.dui;

public class DRowPanel extends DPanel {
    public DRowPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void calcLayout() {
        int rootX = 0;
        for (var child : children) {
            child.y = rootX;
            child.y = 0;
            rootX += child.width;
        }
    }
}
