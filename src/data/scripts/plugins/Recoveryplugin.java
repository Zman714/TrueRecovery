package data.scripts.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;

import java.util.HashSet;
import java.util.List;
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

        List<IntelInfoPlugin> existing = Global.getSector().getIntelManager()
                .getIntel(TrueRecoveryStatusIntel.class);
        if (existing.isEmpty()) {
            Global.getSector().getIntelManager().addIntel(new TrueRecoveryStatusIntel(), false);
        }
    }

    static void applySettings() {
        if (Global.getSector() == null || Global.getSector().getPlayerPerson() == null) return;

        float skillLevel = Global.getSector().getPlayerPerson().getStats()
            .getSkillLevel("automated_ships");
        boolean skilled = skillLevel >= 1f;

        Set<String> recoveryTags = Misc.getAllowedRecoveryTags();

        setAllowed(recoveryTags, "truerecovery_threat",   skilled && isEnabled("truerecovery_threatEnabled"));
        setAllowed(recoveryTags, "truerecovery_dweller", skilled && isEnabled("truerecovery_shroudedEnabled"));
        setAllowed(recoveryTags, "truerecovery_omega",   skilled && isEnabled("truerecovery_omegaEnabled"));
        setAllowed(recoveryTags, "truerecovery_derelict", skilled && isEnabled("truerecovery_derelictEnabled"));

        boolean blueprintifyPresent = Global.getSettings().getModManager().isModEnabled("blueprintify");
        boolean blueprintifyEnabled = blueprintifyPresent && isEnabled("truerecovery_blueprintifyEnabled");
        setAllowed(recoveryTags, "truerecovery_blueprintify",      blueprintifyEnabled);
        setAllowed(recoveryTags, "truerecovery_blueprintify_auto", blueprintifyEnabled && skilled);
        if (blueprintifyEnabled) tagBlueprintifyShips();
    }

    private static void tagBlueprintifyShips() {
        if (Global.getSector() == null) return;

        Set<String> marketFactionIds = new HashSet<>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            marketFactionIds.add(market.getFactionId());
        }

        Set<String> marketKnown = new HashSet<>();
        Set<String> entityKnown = new HashSet<>();
        for (FactionAPI f : Global.getSector().getAllFactions()) {
            if (marketFactionIds.contains(f.getId())) {
                marketKnown.addAll(f.getKnownShips());
            } else {
                entityKnown.addAll(f.getKnownShips());
            }
        }

        for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
            if (spec.isDHull()) continue;
            if (spec.getHullSize() == ShipAPI.HullSize.FIGHTER) continue;
            if (!spec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.UNBOARDABLE)) continue;
            if (!entityKnown.contains(spec.getHullId())) continue;
            if (spec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.STATION)) continue;
            if (spec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.MODULE)) continue;
            if (spec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.UNDER_PARENT)) continue;
            if (spec.hasTag("no_drop")) continue;
            if (spec.hasTag("hide_in_codex")) continue;
            if (spec.getOrdnancePoints(null) == 0) continue;
            String name = spec.getHullName();
            if (name == null || name.replace("?", "").trim().isEmpty()) continue;
            if (marketKnown.contains(spec.getHullId())) continue;
            if (spec.hasTag("truerecovery_threat") || spec.hasTag("truerecovery_dweller")
                    || spec.hasTag("truerecovery_omega") || spec.hasTag("truerecovery_derelict")) continue;

            boolean requiresAcquisition = entityKnown.contains(spec.getHullId())
                    || spec.hasTag("no_bp_drop")
                    || spec.hasTag("no_dealer")
                    || spec.getHints().contains(ShipHullSpecAPI.ShipTypeHints.HIDE_IN_CODEX)
                    || spec.hasTag("codex_unlockable")
                    || spec.hasTag("limited_tooltip_if_locked");

            if (requiresAcquisition) {
                String tag = spec.hasTag("auto_rec")
                        ? "truerecovery_blueprintify_auto"
                        : "truerecovery_blueprintify";
                if (!spec.hasTag(tag)) spec.addTag(tag);
            }
        }
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
