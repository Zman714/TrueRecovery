package data.scripts.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import java.util.Arrays;
import java.util.List;

public class ThreatRecoveryplugin extends BaseModPlugin {

    static final List<String> THREAT_HULL_IDS = Arrays.asList(
        "assault_unit", "fabricator_unit", "hive_unit",
        "overseer_unit", "skirmish_unit", "standoff_unit"
    );

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().addScript(new AutomatedshipsSkillWatcher());
        if (Global.getSector().getPlayerPerson() == null) return;
        float skillLevel = Global.getSector().getPlayerPerson().getStats()
            .getSkillLevel("automated_ships");
        setThreatRecoverable(skillLevel >= 2);
        ShroudedRecoveryplugin.setShroudedRecoverable(skillLevel >= 2);
    }

    static void setThreatRecoverable(boolean recoverable) {
        for (String hullId : THREAT_HULL_IDS) {
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
