package elan.tweaks.config.gui;

import cpw.mods.fml.client.IModGuiFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.Set;

/**
 * Registers the in-game config screen for this mod. Referenced by name from the {@code @Mod}
 * annotation's {@code guiFactory} parameter, so the "Config" button appears next to the mod
 * in the Mods list (Main Menu / in-game menu -> Mods -> Thaumcraft Research Tweaks -> Config).
 *
 * Client-only: FML only instantiates a mod's GUI factory on the physical client, so the
 * client-only classes referenced here are never touched on a dedicated server.
 */
public class ResearchTweaksGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {
        // no runtime setup needed
    }

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return ResearchTweaksConfigGui.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
}
