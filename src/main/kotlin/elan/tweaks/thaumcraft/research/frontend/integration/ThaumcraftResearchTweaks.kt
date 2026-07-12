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
    // Deliberately pinned to the upstream version this fork is based on, rather than MODVER (the git
    // tag). Forge's handshake checks the mod list in BOTH directions: acceptableRemoteVersions below
    // tells our client to accept whatever the server has, but the SERVER also checks us against its
    // own copy of this mod, whose acceptableRemoteVersions defaults to "must match exactly". A server
    // running stock 1.4.0 will therefore refuse a client reporting "1.4.5-Hint", and nothing we set
    // on our side can override a check running on theirs.
    //
    // This fork only adds client-side rendering to a GUI both sides already agree on, so reporting the
    // upstream version is accurate about the thing the handshake actually cares about: protocol
    // compatibility. Bump this if the pack ever ships a newer research-tweaks.
    version = UPSTREAM_VERSION,
    guiFactory = "elan.tweaks.config.gui.ResearchTweaksGuiFactory",
    acceptableRemoteVersions = "*",
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
    dependencies = "required-after:forgelin;required-after:spongemixins;required-after:Thaumcraft;")
object ThaumcraftResearchTweaks {

  const val MOD_ID = "ThaumcraftResearchTweaks"

  /**
   * The upstream research-tweaks version this fork tracks, and the version reported to Forge during
   * the server handshake. It must match the version the server is running, or the server will reject
   * the connection on a mod-list mismatch.
   */
  const val UPSTREAM_VERSION = "1.4.0"

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
