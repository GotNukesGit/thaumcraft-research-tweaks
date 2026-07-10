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
 * Research connection helper (bidirectional).
 *
 * Resolves the aspect currently under the cursor from either place it can live:
 *  - an occupied research hex in the minigame grid (a fixed root / "research point" or a placed node), or
 *  - a discovered aspect cell in one of the side pallets ("potential node").
 *
 * Given that hovered aspect, it highlights everything that shares a trait with it
 * (per [AspectsTreePort.areRelated]) in BOTH areas at once:
 *  - side-pallet cells whose aspect can connect, and
 *  - grid hexes whose placed/root aspect can connect.
 *
 * So hovering a placed node shows which pallet aspects you could drop next AND which other
 * nodes it relates to; hovering a pallet aspect shows which grid nodes it could attach to AND
 * which other pallet aspects share a component with it.
 *
 * Highlight tiers / strength are configurable via [ResearchTweaksConfig]:
 *  - usable: connectable and available -> [ClientConfig.connectionHintColor], drawn
 *    [ClientConfig.connectionHintIntensity] times so the glow stacks and reads clearly.
 *  - drained: connectable but drained / not yet combined -> [ClientConfig.connectionHintDrainedColor]
 *    (single pass, and only when [ClientConfig.connectionHintShowDrained] is enabled).
 *
 * Registered as a [BackgroundUIComponent] ahead of the pallet components so the glow renders
 * behind the aspect icons rather than covering them.
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

    highlightConnectablePalletCells(hoveredAspect, context)
    highlightConnectableHexes(hoveredAspect, context)
  }

  /**
   * Aspect under the cursor, resolved from a side pallet cell first, then from an occupied
   * research hex. Vacant hexes and empty space resolve to null (no hint).
   */
  private fun hoveredAspectAt(uiMousePosition: VectorXY): Aspect? =
      palletGrids.firstNotNullOfOrNull { grid -> grid[uiMousePosition] }
          ?: (hexLayout[uiMousePosition] as? AspectHex.Occupied)?.aspect

  private fun highlightConnectablePalletCells(hoveredAspect: Aspect, context: UIContext) {
    palletGrids.forEach { grid ->
      grid.asOriginList().forEach { (cellOrigin, aspect) ->
        if (!tree.areRelated(hoveredAspect, aspect)) return@forEach

        val drained = pallet.isDrainedOf(aspect)
        if (drained && !config.connectionHintShowDrained) return@forEach

        val center = cellOrigin + cellCenterOffset
        if (drained) glow(center, config.connectionHintDrainedColor, passes = 1, context)
        else glow(center, config.connectionHintColor, config.connectionHintIntensity, context)
      }
    }
  }

  private fun highlightConnectableHexes(hoveredAspect: Aspect, context: UIContext) {
    hexLayout.asOriginList().forEach { (_, hex) ->
      if (hex !is AspectHex.Occupied) return@forEach
      if (!tree.areRelated(hoveredAspect, hex.aspect)) return@forEach

      glow(hex.uiCenter, config.connectionHintColor, config.connectionHintIntensity, context)
    }
  }

  /** Draws the orb glow [passes] times at the same point; stacking additively intensifies it. */
  private fun glow(center: VectorXY, color: Int, passes: Int, context: UIContext) {
    repeat(passes.coerceAtLeast(1)) { context.drawOrb(center, color) }
  }
}
