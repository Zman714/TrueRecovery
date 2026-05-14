package data.scripts.plugins;

import lunalib.lunaSettings.LunaSettingsListener;

class RecoverySettingsListener implements LunaSettingsListener {

    @Override
    public void settingsChanged(String modID) {
        if (!"TrueRecovery".equals(modID)) return;
        Recoveryplugin.applySettings();
    }
}
