package data.scripts.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;

import java.util.Set;

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

        Set<String> recoveryTags = Misc.getAllowedRecoveryTags();

        setAllowed(recoveryTags, "truerecovery_threat",  skilled && isEnabled("truerecovery_threatEnabled"));
        setAllowed(recoveryTags, "truerecovery_dweller", skilled && isEnabled("truerecovery_shroudedEnabled"));
        setAllowed(recoveryTags, "truerecovery_omega",   skilled && isEnabled("truerecovery_omegaEnabled"));
    }

    private static void setAllowed(Set<String> recoveryTags, String tag, boolean allowed) {
        if (allowed) {
            recoveryTags.add(tag);
        } else {
            recoveryTags.remove(tag);
        }
    }

    private static boolean isEnabled(String fieldID) {
        Boolean value = LunaSettings.getBoolean("TrueRecovery", fieldID);
        return value == null || value;
    }
}
