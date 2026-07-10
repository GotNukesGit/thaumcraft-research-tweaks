package elan.tweaks.config.gui;

import net.minecraftforge.common.config.ConfigElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import elan.tweaks.config.ResearchTweaksConfig;
import elan.tweaks.thaumcraft.research.frontend.integration.ThaumcraftResearchTweaks;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

/**
 * The in-game options screen for Thaumcraft Research Tweaks. Lists every property in the mod's
 * "client" config category as an editable control (toggles, an intensity slider, and cycle
 * buttons for the named highlight colors). When the player clicks Done, FML fires an
 * OnConfigChangedEvent for this mod id, which the client listener uses to apply the changes live.
 */
public class ResearchTweaksConfigGui extends GuiConfig {

    public ResearchTweaksConfigGui(GuiScreen parentScreen) {
        super(
            parentScreen,
            clientCategoryElements(),
            ThaumcraftResearchTweaks.MOD_ID,
            false, // does not require a world restart
            false, // does not require a Minecraft restart
            "Thaumcraft Research Tweaks");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<IConfigElement> clientCategoryElements() {
        return new ConfigElement(
            ResearchTweaksConfig.INSTANCE.getConfiguration()
                .getCategory(ResearchTweaksConfig.CATEGORY_CLIENT))
            .getChildElements();
    }
}
