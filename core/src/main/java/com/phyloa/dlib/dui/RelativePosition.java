package com.phyloa.dlib.dui;

import jp.objectclub.vecmath.Vector2f;

public enum RelativePosition {
    TOP_LEFT(0, 0, 0, 0),
    TOP_CENTER(0.5f, 0, 0.5f, 0),
    TOP_RIGHT(1, 0, 1, 0),
    CENTER_LEFT(0, 0.5f, 0, 0.5f),
    CENTER(0.5f, 0.5f, 0.5f, 0.5f),
    CENTER_RIGHT(1, 0.5f, 1, 0.5f),
    BOTTOM_LEFT(0, 1, 0, 1),
    BOTTOM_CENTER(0.5f, 1, 0.5f, 1),
    BOTTOM_RIGHT(1, 1, 1, 1);

    public float elWidthScale;
    public float elHeightScale;
    public float parentWidthScale;
    public float parentHeightScale;

    RelativePosition(float elWidthScale, float elHeightScale, float parentWidthScale, float parentHeightScale) {
        this.elWidthScale = elWidthScale;
        this.elHeightScale = elHeightScale;
        this.parentWidthScale = parentWidthScale;
        this.parentHeightScale = parentHeightScale;
    }

    public Vector2f calcPos(DUIElement el, DUIElement parent) {
        return new Vector2f(
                parent.width * parentWidthScale - el.width * elWidthScale + el.relX,
                parent.height * parentHeightScale - el.height * elHeightScale + el.relY);
    }
}
