package elan.tweaks.thaumcraft.research.frontend.integration

import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.SidedProxy
import cpw.mods.fml.common.event.FMLLoadCompleteEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.GameRegistry
import elan.tweaks.config.ResearchTweaksConfig
import elan.tweaks.thaumcraft.research.frontend.integration.ThaumcraftResearchTweaks.MOD_ID
import elan.tweaks.thaumcraft.research.frontend.integration.proxies.SingletonInitializer
import elan.tweaks.thaumcraft.research.frontend.integration.table.ThaumcraftResearchGuiHandler
import net.minecraftforge.common.config.Configuration

@Mod(
    modid = MOD_ID,
    name = "Thaumcraft Research Tweaks",
    version = MODVER,
    guiFactory = "elan.tweaks.config.gui.ResearchTweaksGuiFactory",
    acceptableRemoteVersions = "*",
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
    dependencies = "required-after:forgelin;required-after:spongemixins;required-after:Thaumcraft;")
object ThaumcraftResearchTweaks {

  const val MOD_ID = "ThaumcraftResearchTweaks"

  @SidedProxy(
      clientSide =
          "elan.tweaks.thaumcraft.research.frontend.integration.proxies.ClientSingletonInitializer",
      serverSide =
          "elan.tweaks.thaumcraft.research.frontend.integration.proxies.ServerSingletonInitializer")
  lateinit var singletonInitializer: SingletonInitializer

  @Mod.EventHandler
  fun onInit(event: FMLLoadCompleteEvent) {
    NetworkRegistry.INSTANCE.registerGuiHandler(
        ThaumcraftResearchTweaks, ThaumcraftResearchGuiHandler())

    singletonInitializer.initialize()
  }
    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        ResearchTweaksConfig.loadConfig(Configuration(event.suggestedConfigurationFile))
    }
}
