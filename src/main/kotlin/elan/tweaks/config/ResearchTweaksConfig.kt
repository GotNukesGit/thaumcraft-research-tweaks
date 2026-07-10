package elan.tweaks.config

import net.minecraftforge.common.config.Configuration

public enum class AspectSortingOptions {
    SIMPLE_TO_COMPLEX,
    ALPHABETICAL_FILL_LEFT,
    ALPHABETICAL_BALANCED,
}

object ResearchTweaksConfig {
    val client = ClientConfig()

    fun loadConfig(config: Configuration) {
        config.load()
        client.allowCombiningSameAspect = config.get("client", "allowCombiningSameAspect", false, "Should an aspect be allowed to be dragged onto itself? (e.g. Ignis onto Ignis)").boolean
        client.researchTableSortingOrder = config.getInt("researchTableSortingOrder", "client", 0, 0,
            AspectSortingOptions.values().size - 1,
            "How should the aspects be ordered?\n" +
                "0 = SIMPLE_TO_COMPLEX - Aspects that take more combining are listed towards the bottom\n" +
                "1 = ALPHABETICAL_FILL_LEFT - Sorted alphabetically, tries to fill the entire left side before right\n" +
                "2 = ALPHABETICAL_BALANCED - Sorted alphabetically, left will have the first half of all aspects,\n" +
                "   right will have the second half\n")
        client.connectionHintEnabled = config.get("client", "connectionHintEnabled", true,
            "When hovering an occupied research hex (a root aspect or a placed node), highlight the\n" +
                "discovered aspects in the side pallets that can legally connect to it, so you can see\n" +
                "at a glance which aspects you could place next to continue the chain.").boolean
        client.connectionHintShowDrained = config.get("client", "connectionHintShowDrained", true,
            "Also (dimly) highlight connectable aspects you have currently drained / not yet combined,\n" +
                "so you know which aspects are relevant even if you must derive more of them first.").boolean
        client.connectionHintColor = config.getInt("connectionHintColor", "client", 0xF0E68C, 0x000000, 0xFFFFFF,
            "RGB color (hex, e.g. F0E68C) of the highlight glow for usable connectable aspects.")
        client.connectionHintDrainedColor = config.getInt("connectionHintDrainedColor", "client", 0x4D4030, 0x000000, 0xFFFFFF,
            "RGB color (hex) of the dim highlight for connectable-but-drained aspects (see connectionHintShowDrained).")
        config.save()
    }

    class ClientConfig {
        var allowCombiningSameAspect: Boolean = false
        var researchTableSortingOrder: Int = AspectSortingOptions.SIMPLE_TO_COMPLEX.ordinal
        var connectionHintEnabled: Boolean = true
        var connectionHintShowDrained: Boolean = true
        var connectionHintColor: Int = 0xF0E68C
        var connectionHintDrainedColor: Int = 0x4D4030
    }
}
