package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import java.util.Arrays;
import java.util.List;

class ShroudedRecoveryplugin {

    private ShroudedRecoveryplugin() {}

    static final List<String> SHROUDED_HULL_IDS = Arrays.asList(
        "shrouded_tendril", "shrouded_eye", "shrouded_maelstrom", "shrouded_maw"
    );

    static void setShroudedRecoverable(boolean recoverable) {
        for (String hullId : SHROUDED_HULL_IDS) {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullId);
            if (spec == null) continue;
            if (recoverable) {
                spec.getHints().remove(ShipTypeHints.UNBOARDABLE);
            } else {
                spec.getHints().add(ShipTypeHints.UNBOARDABLE);
            }
        }
    }
}
