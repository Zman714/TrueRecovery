package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CutStyle;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.Color;
import java.util.*;

public class TrueRecoveryStatusIntel extends BaseIntelPlugin {

    private static final String BTN_PREV = "tr_prev";
    private static final String BTN_NEXT = "tr_next";
    private static final int PAGE_SIZE   = 50;

    private static final String[] TAGS = {
        "truerecovery_threat",
        "truerecovery_dweller",
        "truerecovery_omega",
        "truerecovery_derelict",
        "truerecovery_blueprintify_auto",
        "truerecovery_blueprintify",
    };

    private static final String[] LABELS = {
        "Threat (Requires Automated Ships)",
        "Shrouded Dweller (Requires Automated Ships)",
        "Omega (Requires Automated Ships)",
        "Derelict (Requires Automated Ships)",
        "Blueprintify Compat — Automated (Requires Automated Ships)",
        "Blueprintify Compat",
    };

    private static final boolean[] BLUEPRINTIFY_ONLY = {
        false, false, false, false, true, true
    };

    private static class ShipEntry {
        final String hullId;
        final String hullName;
        final String manufacturer;
        final String category;

        ShipEntry(String hullId, String hullName, String manufacturer, String category) {
            this.hullId       = hullId;
            this.hullName     = (hullName != null && !hullName.isEmpty()) ? hullName : hullId;
            this.manufacturer = manufacturer != null ? manufacturer : "";
            this.category     = category;
        }
    }

    private List<ShipEntry>       allEntries     = new ArrayList<>();
    private Map<String, Integer>  categoryCounts = new LinkedHashMap<>();
    private int page = 0;

    public TrueRecoveryStatusIntel() {
        refresh();
    }

    void refresh() {
        allEntries     = new ArrayList<>();
        categoryCounts = new LinkedHashMap<>();

        boolean blueprintifyPresent = Global.getSettings().getModManager().isModEnabled("blueprintify");

        for (int i = 0; i < TAGS.length; i++) {
            if (BLUEPRINTIFY_ONLY[i] && !blueprintifyPresent) continue;

            String tag   = TAGS[i];
            String label = LABELS[i];

            List<ShipEntry> group = new ArrayList<>();
            for (ShipHullSpecAPI spec : Global.getSettings().getAllShipHullSpecs()) {
                if (!spec.hasTag(tag)) continue;
                group.add(new ShipEntry(spec.getHullId(), spec.getHullName(), spec.getManufacturer(), label));
            }

            Collections.sort(group, new Comparator<ShipEntry>() {
                public int compare(ShipEntry a, ShipEntry b) {
                    int c = a.manufacturer.compareToIgnoreCase(b.manufacturer);
                    return c != 0 ? c : a.hullName.compareToIgnoreCase(b.hullName);
                }
            });

            categoryCounts.put(label, group.size());
            allEntries.addAll(group);
        }
    }

    @Override protected String getName() { return "TrueRecovery"; }
    @Override public String getIcon() { return "graphics/icons/intel/codex_update.png"; }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
        info.addPara("%s ships tagged as recoverable.", 3f, Misc.getHighlightColor(),
                String.valueOf(allEntries.size()));
    }

    @Override public boolean hasLargeDescription() { return true; }
    @Override public boolean hasSmallDescription() { return false; }
    @Override public boolean isEnding() { return false; }
    @Override public boolean isEnded() { return false; }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if (BTN_PREV.equals(buttonId)) {
            page = Math.max(0, page - 1);
        } else if (BTN_NEXT.equals(buttonId)) {
            page++;
        }
        ui.recreateIntelUI();
    }

    @Override
    public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
        refresh();

        float opad = 10f;
        float pad  = 3f;

        int total      = allEntries.size();
        int totalPages = total == 0 ? 1 : (total + PAGE_SIZE - 1) / PAGE_SIZE;
        int p          = Math.max(0, Math.min(page, totalPages - 1));
        int start      = p * PAGE_SIZE;
        int end        = Math.min(start + PAGE_SIZE, total);

        TooltipMakerAPI content = panel.createUIElement(width, height, true);
        content.addPara(
            "The following ships have been tagged as recoverable by TrueRecovery. "
            + "Entries marked \"Requires Automated Ships\" can only be recovered once that skill is unlocked.",
            opad
        );

        content.addSectionHeading(
            "Ships  (" + total + ")  —  page " + (p + 1) + " of " + totalPages,
            Alignment.LMID, opad
        );
        addPageNav(content, p, totalPages, pad);

        if (total == 0) {
            content.addPara("None", Misc.getGrayColor(), pad);
        } else {
            String lastCategory = null;
            if (start > 0 && allEntries.get(start).category.equals(allEntries.get(start - 1).category)) {
                lastCategory = allEntries.get(start).category;
                content.addSectionHeading(
                    lastCategory + " (" + categoryCounts.get(lastCategory) + ")  —  continued",
                    Alignment.LMID, opad
                );
            }

            for (int i = start; i < end; i++) {
                ShipEntry e = allEntries.get(i);
                if (!e.category.equals(lastCategory)) {
                    lastCategory = e.category;
                    content.addSectionHeading(
                        lastCategory + " (" + categoryCounts.get(lastCategory) + ")",
                        Alignment.LMID, opad
                    );
                }
                Color color = getEntryColor(e);
                String line = e.hullName + "  [" + e.hullId + "]";
                if (!e.manufacturer.isEmpty()) line += "  —  " + e.manufacturer;
                if (color != null) {
                    content.addPara(line, pad, color, e.manufacturer);
                } else {
                    content.addPara(line, Misc.getGrayColor(), pad);
                }
            }
        }

        addPageNav(content, p, totalPages, opad);
        panel.addUIElement(content).inTL(0, 0);
    }

    private Color getEntryColor(ShipEntry e) {
        if (!e.manufacturer.isEmpty() && Global.getSettings().hasDesignTypeColor(e.manufacturer)) {
            return Global.getSettings().getDesignTypeColor(e.manufacturer);
        }
        return null;
    }

    private void addPageNav(TooltipMakerAPI info, int page, int totalPages, float pad) {
        if (totalPages <= 1) return;
        if (page > 0) {
            info.addButton("< Previous", BTN_PREV,
                    Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(),
                    Alignment.MID, CutStyle.C2_MENU, 110f, 20f, 0f);
        }
        if (page < totalPages - 1) {
            info.addButton("Next >", BTN_NEXT,
                    Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(),
                    Alignment.MID, CutStyle.C2_MENU, 110f, 20f, 10f);
        }
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = new HashSet<>();
        tags.add("Personal");
        return tags;
    }
}
