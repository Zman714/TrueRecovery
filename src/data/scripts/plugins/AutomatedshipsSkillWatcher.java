package data.scripts.plugins;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;

public class AutomatedshipsSkillWatcher implements EveryFrameScript {

    private float lastSkillLevel = -1f;

    @Override
    public boolean isDone() { return false; }

    @Override
    public boolean runWhilePaused() { return false; }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null || Global.getSector().getPlayerPerson() == null) return;

        float skillLevel = Global.getSector().getPlayerPerson().getStats()
            .getSkillLevel("automated_ships");

        if (skillLevel != lastSkillLevel) {
            lastSkillLevel = skillLevel;
            ThreatRecoveryplugin.setThreatRecoverable(skillLevel > 0);
            ShroudedRecoveryplugin.setShroudedRecoverable(skillLevel >= 2);
        }
    }
}
