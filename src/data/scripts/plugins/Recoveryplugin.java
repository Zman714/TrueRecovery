package data.scripts.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import java.util.Arrays;
import java.util.List;

public class Recoveryplugin extends BaseModPlugin {

    static final List<String> THREAT_HULL_IDS = Arrays.asList(
        "assault_unit", "fabricator_unit", "hive_unit",
        "overseer_unit", "skirmish_unit", "standoff_unit"
    );

    @Override
    public void onGameLoad(boolean newGame) {
        boolean hasSkill = Global.getSector().getPlayerPerson().getStats()
            .getSkillLevel("automated_ships") > 0;
        setThreatRecoverable(hasSkill);
        Global.getSector().addScript(new ThreatSkillWatcher());
    }

    static void setThreatRecoverable(boolean recoverable) {
        for (String hullId : THREAT_HULL_IDS) {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullId);
            if (spec == null) continue;
            if (recoverable) {
                spec.getHints().remove(ShipHullSpecAPI.ShipTypeHints.UNBOARDABLE);
            } else if (!spec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.UNBOARDABLE)) {
                spec.getHints().add(ShipHullSpecAPI.ShipTypeHints.UNBOARDABLE);
            }
        }
    }
}
