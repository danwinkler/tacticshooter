package com.phyloa.dlib.dui;

/**
 * Forces its children into a grid
 */
public class DGrid extends DPanel {
    int cols;
    int rows;
    DUIElement[][] grid;

    public DGrid(int x, int y, int width, int height, int cols, int rows) {
        super(x, y, width, height);
        this.cols = cols;
        this.rows = rows;

        grid = new DUIElement[cols][rows];
    }

    public void add(DUIElement e, int col, int row) {
        grid[col][row] = e;
        e.x = col * (width / cols);
        e.y = row * (height / rows);
        e.width = width / cols;
        e.height = height / rows;
        super.add(e);
    }

    public void add(DUIElement e) {
        throw new RuntimeException("Call add(DUIElement e, int col, int row) instead");
    }

    @Override
    public void calcLayout(DUI dui) {
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                DUIElement e = grid[i][j];
                if (e != null) {
                    e.x = i * (width / cols);
                    e.y = j * (height / rows);
                    e.width = width / cols;
                    e.height = height / rows;
                    e.calcLayout(dui);
                }
            }
        }
    }
}
