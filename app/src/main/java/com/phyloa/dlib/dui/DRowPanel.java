package com.phyloa.dlib.dui;

public class DRowPanel extends DPanel {
    public DRowPanel() {
        super();
    }

    public DRowPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void calcLayout(DUI dui) {
        int rootX = 0;
        int maxHeight = 0;
        for (var child : children) {
            child.x = rootX;
            child.y = 0;
            rootX += child.width;
            maxHeight = Math.max(maxHeight, child.height);
        }
        width = rootX;
        height = maxHeight;
    }

    public void addSpacer(int pixels) {
        add(new DSpacer(pixels, 0));
    }
}
