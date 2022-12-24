package com.phyloa.dlib.dui;

public class DColumnPanel extends DPanel {
    public DColumnPanel() {
        super(0, 0, 0, 0);
    }

    public DColumnPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public DColumnPanel(RelativePosition relative, int relX, int relY) {
        super(0, 0, 0, 0);
        this.setRelativePosition(relative, relX, relY);
    }

    @Override
    protected void calcLayout(DUI dui) {
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

        // Now that we know the dimensions of the column, we can center children that
        // requested it
        for (var child : children) {
            if (child.relative == RelativePosition.CENTER) {
                child.x = (width - child.width) / 2;
            }
        }
    }

    public void addSpacer(int pixels) {
        add(new DSpacer(0, pixels));
    }
}
