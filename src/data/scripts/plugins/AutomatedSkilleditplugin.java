package data.scripts.plugins;

import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.LevelBasedEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;

public class AutomatedSkilleditplugin {

    public static class Level2 implements CharacterStatsSkillEffect {

        @Override
        public void apply(MutableCharacterStatsAPI stats, String id, float level) {}

        @Override
        public void unapply(MutableCharacterStatsAPI stats, String id) {}

        @Override
        public String getEffectDescription(float level) {
            return "Threat and Shrouded Dweller ships can be recovered from the battlefield";
        }

        @Override
        public String getEffectPerLevelDescription() { return ""; }

        @Override
        public LevelBasedEffect.ScopeDescription getScopeDescription() { return LevelBasedEffect.ScopeDescription.NONE; }
    }
}
