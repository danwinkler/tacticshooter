package com.danwink.tacticshooter;

import org.newdawn.slick.Font;

public class UIHelper {
    public static int getUIScale(float height) {
        if (height < 1300) {
            return 1;
        } else if (height < 2000) {
            return 2;
        } else {
            return 3;
        }
    }

    public static Font getFontForScale(int scale) {
        return StaticFiles.fonts[scale - 1];
    }
}
