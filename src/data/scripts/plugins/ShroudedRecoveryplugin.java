package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import java.util.Arrays;
import java.util.List;

public class ShroudedRecoveryplugin {

    static final List<String> SHROUDED_HULL_IDS = Arrays.asList(
        "shrouded_tendril", "shrouded_eye", "shrouded_maelstrom", "shrouded_maw"
    );

    static void setShroudedRecoverable(boolean recoverable) {
        for (String hullId : SHROUDED_HULL_IDS) {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullId);
            if (spec == null) continue;
            if (recoverable) {
                spec.getTags().remove("no_battle_salvage");
            } else if (!spec.getTags().contains("no_battle_salvage")) {
                spec.getTags().add("no_battle_salvage");
            }
        }
    }
}
