package de.rhm176.modmenu.compat;

import de.rhm176.modmenu.api.ModConfigPanelFactory;
import de.rhm176.modmenu.api.ModMenuApi;

// totally not cursed
public class ModMenuModMenuCompat implements ModMenuApi {
    @Override
    public ModConfigPanelFactory getModConfigPanelFactory() {
        return ModMenuConfigPanel::new;
    }
}
