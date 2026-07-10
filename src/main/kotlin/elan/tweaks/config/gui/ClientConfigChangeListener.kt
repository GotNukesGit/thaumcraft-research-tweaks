package elan.tweaks.config.gui

import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import elan.tweaks.config.ResearchTweaksConfig
import elan.tweaks.thaumcraft.research.frontend.integration.ThaumcraftResearchTweaks

/**
 * Applies config changes live. When the player edits settings via the in-game config screen and
 * clicks Done, FML posts an [OnConfigChangedEvent]; on a match for this mod id we re-read the
 * values into the running config so the research-table highlight updates without a restart.
 *
 * Registered on the FML event bus from the client proxy only, since [OnConfigChangedEvent] is a
 * client-side class and this mod also loads on dedicated servers.
 */
object ClientConfigChangeListener {

  @SubscribeEvent
  fun onConfigChanged(event: OnConfigChangedEvent) {
    if (event.modID == ThaumcraftResearchTweaks.MOD_ID) {
      ResearchTweaksConfig.reload()
    }
  }
}
