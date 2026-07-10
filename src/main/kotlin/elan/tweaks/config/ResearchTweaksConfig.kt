package elan.tweaks.config

import net.minecraftforge.common.config.Configuration

public enum class AspectSortingOptions {
    SIMPLE_TO_COMPLEX,
    ALPHABETICAL_FILL_LEFT,
    ALPHABETICAL_BALANCED,
}

/**
 * Named highlight colors so the in-game config screen can show a friendly cycle button
 * instead of asking the player to type a raw color number.
 */
enum class HighlightColor(val rgb: Int) {
    WHITE(0xFFFFFF),
    GOLD(0xFFD700),
    YELLOW(0xFFF04D),
    ORANGE(0xFF9C33),
    CYAN(0x33F0FF),
    GREEN(0x5DFF5D),
    PINK(0xFF6EC7),
    RED(0xFF5555),
    BLUE(0x6E9CFF),
    DIM(0x4D4030);

    companion object {
        fun names(): Array<String> = values().map { it.name }.toTypedArray()

        fun rgbOf(name: String, fallback: HighlightColor): Int =
            values().firstOrNull { it.name.equals(name, ignoreCase = true) }?.rgb ?: fallback.rgb
    }
}

object ResearchTweaksConfig {
    const val CATEGORY_CLIENT = "client"

    val client = ClientConfig()

    lateinit var configuration: Configuration
        private set

    fun loadConfig(config: Configuration) {
        configuration = config
        config.load()
        readValues(config)
        if (config.hasChanged()) config.save()
    }

    /** Re-read values from the (already in-memory) configuration; used after in-game edits. */
    fun reload() {
        if (!::configuration.isInitialized) return
        readValues(configuration)
        if (configuration.hasChanged()) configuration.save()
    }

    private fun readValues(config: Configuration) {
        client.allowCombiningSameAspect =
            config.get(CATEGORY_CLIENT, "allowCombiningSameAspect", false,
                "Should an aspect be allowed to be dragged onto itself? (e.g. Ignis onto Ignis)").boolean

        client.researchTableSortingOrder =
            config.getInt("researchTableSortingOrder", CATEGORY_CLIENT, 0, 0,
                AspectSortingOptions.values().size - 1,
                "How should the aspects be ordered?\n" +
                    "0 = SIMPLE_TO_COMPLEX - Aspects that take more combining are listed towards the bottom\n" +
                    "1 = ALPHABETICAL_FILL_LEFT - Sorted alphabetically, tries to fill the entire left side before right\n" +
                    "2 = ALPHABETICAL_BALANCED - Sorted alphabetically, left will have the first half of all aspects,\n" +
                    "   right will have the second half\n")

        client.connectionHintEnabled =
            config.get(CATEGORY_CLIENT, "connectionHintEnabled", true,
                "Highlight, on hover, the aspects that can connect to the one under the cursor.\n" +
                    "Works both ways: hover a research hex to see usable pallet aspects, or hover a\n" +
                    "pallet aspect to see the hexes / aspects it can link to.").boolean

        client.connectionHintShowDrained =
            config.get(CATEGORY_CLIENT, "connectionHintShowDrained", true,
                "Also dimly highlight connectable aspects you have currently drained / not yet combined.").boolean

        client.connectionHintIntensity =
            config.getInt("connectionHintIntensity", CATEGORY_CLIENT, 3, 1, 8,
                "How strong the usable-aspect highlight glow is (it is drawn this many times and stacks).\n" +
                    "1 = subtle, 3 = default, 6+ = very bold.")

        client.connectionHintColor = readColor(config, "connectionHintColor",
            HighlightColor.GOLD, "Highlight color for usable connectable aspects.")

        client.connectionHintDrainedColor = readColor(config, "connectionHintDrainedColor",
            HighlightColor.DIM, "Highlight color for connectable-but-drained aspects.")
    }

    private fun readColor(
        config: Configuration,
        key: String,
        default: HighlightColor,
        comment: String
    ): Int {
        val property = config.get(CATEGORY_CLIENT, key, default.name, comment)
        property.validValues = HighlightColor.names()
        return HighlightColor.rgbOf(property.string, default)
    }

    class ClientConfig {
        var allowCombiningSameAspect: Boolean = false
        var researchTableSortingOrder: Int = AspectSortingOptions.SIMPLE_TO_COMPLEX.ordinal
        var connectionHintEnabled: Boolean = true
        var connectionHintShowDrained: Boolean = true
        var connectionHintIntensity: Int = 3
        var connectionHintColor: Int = HighlightColor.GOLD.rgb
        var connectionHintDrainedColor: Int = HighlightColor.DIM.rgb
    }
}
