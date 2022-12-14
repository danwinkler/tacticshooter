package com.danwink.tacticshooter.ai;

import java.util.Comparator;
import java.util.HashMap;

import com.danwink.tacticshooter.gameobjects.Unit.UnitDef;

public class AIUtils {
    public static UnitDef findScoutLikeUnitDef(HashMap<String, UnitDef> defs) {
        return defs.values().stream().max(Comparator.comparing(def -> {
            if (def.name.toLowerCase().contains("scout")) {
                return 10000.f;
            } else {
                return def.speed;
            }
        })).get();
    }

    public static UnitDef findLightUnitLikeDef(HashMap<String, UnitDef> defs) {
        return defs.values().stream().max(Comparator.comparing(def -> {
            // Out of ideas at the moment of a better way to do this lol
            if (def.name.toLowerCase().contains("light")) {
                return 10000.f;
            } else {
                return 0.f;
            }
        })).get();
    }
}
