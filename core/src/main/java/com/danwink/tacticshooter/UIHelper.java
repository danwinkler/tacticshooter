package com.danwink.tacticshooter;

import org.newdawn.slick.Font;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class UIHelper {
    public static int getUIScale(float height) {
        if (height < 1400) {
            return 1;
        } else if (height < 2000) {
            return 2;
        } else {
            return 3;
        }
    }

    public static BitmapFont getFontForScale(int scale) {
        return StaticFiles.fonts[scale - 1];
    }
}
