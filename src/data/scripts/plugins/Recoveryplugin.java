package data.scripts.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import lunalib.lunaSettings.LunaSettings;

public class Recoveryplugin extends BaseModPlugin {

    @Override
    public void onApplicationLoad() {
        LunaSettings.addSettingsListener(new RecoverySettingsListener());
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().addTransientScript(new AutomatedshipsSkillWatcher());
        applySettings();
    }

    static void applySettings() {
        if (Global.getSector() == null || Global.getSector().getPlayerPerson() == null) return;

        float skillLevel = Global.getSector().getPlayerPerson().getStats()
            .getSkillLevel("automated_ships");
        boolean skilled = skillLevel >= 1f;

        boolean threat   = skilled && isEnabled("truerecovery_threatEnabled");
        boolean shrouded = skilled && isEnabled("truerecovery_shroudedEnabled");
        boolean omega    = skilled && isEnabled("truerecovery_omegaEnabled");

        for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
            if (spec.hasTag("threat")) {
                setRecoverable(spec, threat);
            } else if (spec.hasTag("dweller")) {
                setRecoverable(spec, shrouded);
            } else if (spec.hasTag("omega")) {
                setRecoverable(spec, omega);
            }
        }
    }

    private static void setRecoverable(ShipHullSpecAPI spec, boolean recoverable) {
        if (recoverable) {
            spec.getHints().remove(ShipTypeHints.UNBOARDABLE);
        } else if (!spec.getHints().contains(ShipTypeHints.UNBOARDABLE)) {
            spec.getHints().add(ShipTypeHints.UNBOARDABLE);
        }
    }

    private static boolean isEnabled(String fieldID) {
        Boolean value = LunaSettings.getBoolean("TrueRecovery", fieldID);
        return value == null || value;
    }
}
