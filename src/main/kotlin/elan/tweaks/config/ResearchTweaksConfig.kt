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
        config.save()
    }

    class ClientConfig {
        var allowCombiningSameAspect: Boolean = false
        var researchTableSortingOrder: Int = AspectSortingOptions.SIMPLE_TO_COMPLEX.ordinal
    }
}
