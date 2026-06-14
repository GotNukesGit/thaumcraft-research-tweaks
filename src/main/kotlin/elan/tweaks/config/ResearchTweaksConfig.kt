package elan.tweaks.config

import net.minecraftforge.common.config.Configuration


object ResearchTweaksConfig {
    val client = ClientConfig()

    fun loadConfig(config: Configuration) {
        config.load()
        client.allowCombiningSameAspect = config.get("client", "allowCombiningSameAspect", false, "Should an aspect be allowed to be dragged onto itself? (e.g. Ignis onto Ignis)").boolean
        config.save()
    }

    class ClientConfig {
        var allowCombiningSameAspect: Boolean = false
    }
}
