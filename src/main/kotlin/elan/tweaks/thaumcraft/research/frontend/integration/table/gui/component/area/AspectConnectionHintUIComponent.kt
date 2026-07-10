package elan.tweaks.thaumcraft.research.frontend.integration.table.gui.component.area

import elan.tweaks.common.gui.component.BackgroundUIComponent
import elan.tweaks.common.gui.component.UIContext
import elan.tweaks.common.gui.dto.Vector2D
import elan.tweaks.common.gui.dto.VectorXY
import elan.tweaks.common.gui.layout.grid.GridLayout
import elan.tweaks.common.gui.layout.hex.HexLayout
import elan.tweaks.config.ResearchTweaksConfig
import elan.tweaks.thaumcraft.research.frontend.domain.ports.provided.AspectPalletPort
import elan.tweaks.thaumcraft.research.frontend.domain.ports.provided.AspectsTreePort
import elan.tweaks.thaumcraft.research.frontend.domain.ports.provided.ResearchProcessPort
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.dto.AspectHex
import thaumcraft.api.aspects.Aspect

/**
 * Research connection helper.
 *
 * When the player hovers over an occupied research hex — either a fixed root (a "research point")
 * or an already-placed node — this highlights every discovered aspect in the side pallets that can
 * legally connect to the hovered aspect, per [AspectsTreePort.areRelated]. The intent is to answer
 * "which of the aspects I have could I place next to continue this connection?" at a glance, without
 * having to remember the full aspect combination tree.
 *
 * Two highlight tiers (both configurable via [ResearchTweaksConfig]):
 *  - usable: connectable and currently available in the pallet -> [ClientConfig.connectionHintColor]
 *  - drained: connectable but currently drained / not yet combined -> [ClientConfig.connectionHintDrainedColor]
 *    (only shown when [ClientConfig.connectionHintShowDrained] is enabled)
 *
 * This is a [BackgroundUIComponent] and is registered ahead of the pallet components in the GUI
 * factory, so the glow renders behind the aspect icons (mirroring how node orbs are drawn in the
 * hex map) rather than covering them.
 */
class AspectConnectionHintUIComponent(
    private val research: ResearchProcessPort,
    private val tree: AspectsTreePort,
    private val pallet: AspectPalletPort,
    private val hexLayout: HexLayout<AspectHex>,
    private val palletGrids: List<GridLayout<Aspect>>,
    cellSizePixels: Int,
) : BackgroundUIComponent {

  private val cellCenterOffset = Vector2D(cellSizePixels / 2, cellSizePixels / 2)

  private val config
    get() = ResearchTweaksConfig.client

  override fun onDrawBackground(
      uiMousePosition: VectorXY,
      partialTicks: Float,
      context: UIContext
  ) {
    if (!config.connectionHintEnabled) return
    // Nothing to hint at if there is no editable, valid note in progress.
    if (research.missingNotes() || research.notesCorrupted() || research.complete()) return

    val hoveredAspect = hoveredAspectAt(uiMousePosition) ?: return

    palletGrids.forEach { grid -> highlightConnectableCells(grid, hoveredAspect, context) }
  }

  /** Aspect under the cursor, but only when hovering an occupied hex (root or placed node). */
  private fun hoveredAspectAt(uiMousePosition: VectorXY): Aspect? =
      when (val hex = hexLayout[uiMousePosition]) {
        is AspectHex.Occupied -> hex.aspect
        else -> null
      }

  private fun highlightConnectableCells(
      grid: GridLayout<Aspect>,
      hoveredAspect: Aspect,
      context: UIContext
  ) {
    grid.asOriginList().forEach { (cellOrigin, aspect) ->
      if (!tree.areRelated(hoveredAspect, aspect)) return@forEach

      val drained = pallet.isDrainedOf(aspect)
      if (drained && !config.connectionHintShowDrained) return@forEach

      val color = if (drained) config.connectionHintDrainedColor else config.connectionHintColor
      context.drawOrb(cellOrigin + cellCenterOffset, color)
    }
  }
}
