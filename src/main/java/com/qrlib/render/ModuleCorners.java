package com.qrlib.render;

/**
 * Indicates which corners of a module should be rendered rounded. A corner is rounded
 * only when neither of its two adjacent modules is dark, so that connected runs of dark
 * modules merge into a single shape and rounding appears only at their ends.
 */
public final class ModuleCorners {

    public static final ModuleCorners NONE = new ModuleCorners(false, false, false, false);

    private final boolean topLeft;
    private final boolean topRight;
    private final boolean bottomRight;
    private final boolean bottomLeft;

    public ModuleCorners(boolean topLeft, boolean topRight, boolean bottomRight, boolean bottomLeft) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomRight = bottomRight;
        this.bottomLeft = bottomLeft;
    }

    public boolean isTopLeft() {
        return topLeft;
    }

    public boolean isTopRight() {
        return topRight;
    }

    public boolean isBottomRight() {
        return bottomRight;
    }

    public boolean isBottomLeft() {
        return bottomLeft;
    }
}
