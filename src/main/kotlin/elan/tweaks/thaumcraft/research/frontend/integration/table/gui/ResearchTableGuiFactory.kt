package elan.tweaks.thaumcraft.research.frontend.integration.table.gui

import elan.tweaks.common.gui.ComposableContainerGui.Companion.gui
import elan.tweaks.common.gui.component.UIComponent
import elan.tweaks.common.gui.component.texture.TextureBackgroundUIComponent.Companion.background
import elan.tweaks.common.gui.dto.Rectangle
import elan.tweaks.common.gui.dto.Vector2D
import elan.tweaks.common.gui.layout.grid.GridLayout
import elan.tweaks.common.gui.layout.grid.GridLayoutDynamicListAdapter
import elan.tweaks.common.gui.layout.hex.HexLayout
import elan.tweaks.config.AspectSortingOptions
import elan.tweaks.config.ResearchTweaksConfig
import elan.tweaks.thaumcraft.research.frontend.integration.table.TableUIContext
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.component.*
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.component.area.AspectConnectionHintUIComponent
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.component.area.AspectHexMapEditorUIComponent
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.component.area.AspectHexMapUIComponent
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.component.area.ParchmentUIComponent
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.component.area.Runes
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.dto.AspectHex
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.layout.HexLayoutResearchNoteDataAdapter
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.layout.ResearchTableLayout
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.textures.HexTexture
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.textures.PlayerInventoryTexture
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.textures.ResearchTableInventoryTexture
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.textures.ResearchTableInventoryTexture.AspectPools
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.textures.ResearchTableInventoryTexture.CopyButton
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.textures.ResearchTableInventoryTexture.ResearchArea
import elan.tweaks.thaumcraft.research.frontend.integration.table.gui.textures.ResearchTableInventoryTexture.UsageHint
import net.minecraft.entity.player.EntityPlayer
import thaumcraft.api.aspects.Aspect
import thaumcraft.common.tiles.TileResearchTable

object ResearchTableGuiFactory {

  fun create(player: EntityPlayer, table: TileResearchTable) =
      PortContainer(player, table).run {
        val hexLayout = hexLayout()
        val palletGrids = palletGrids()
        gui(
            scale = ResearchTableLayout.guiScale,
            container = inventory,
            components =
                tableAndInventoryBackgrounds() +
                    researchArea(hexLayout) +
                    // Drawn before the pallets so the connection-hint glow renders behind aspect icons.
                    connectionHintComponents(hexLayout, palletGrids) +
                    copyButton() +
                    palletComponents(palletGrids) +
                    UsageHintUIComponent(
                        UsageHint.uiBounds, UsageHint.onMouseOverBounds, researcher) +
                    ScribeToolsNotificationUIComponent(research, ResearchArea.centerOrigin) +
                    AspectDragAndDropUIComponent(pallet) +
                    KnowledgeNotificationUIComponent()) { screenOrigin, fontRenderer ->
          TableUIContext(screenOrigin, fontRenderer)
        }
      }

  private fun tableAndInventoryBackgrounds() =
      listOf(
          background(
              uiOrigin = Vector2D.ZERO,
              texture = ResearchTableInventoryTexture,
          ),
          background(
              uiOrigin = ResearchTableInventoryTexture.inventoryOrigin,
              texture = PlayerInventoryTexture,
          ))

  private fun PortContainer.hexLayout(): HexLayout<AspectHex> =
      HexLayoutResearchNoteDataAdapter(
          bounds = ResearchArea.bounds,
          centerUiOrigin = ResearchArea.centerOrigin,
          hexSize = HexTexture.SIZE_PIXELS,
          aspectTree = tree,
          researcher = researcher,
          researchProcess = research)

  private fun PortContainer.connectionHintComponents(
      hexLayout: HexLayout<AspectHex>,
      palletGrids: List<GridLayout<Aspect>>
  ): List<UIComponent> =
      listOf(
          AspectConnectionHintUIComponent(
              research = research,
              tree = tree,
              pallet = pallet,
              hexLayout = hexLayout,
              palletGrids = palletGrids,
              cellSizePixels = AspectPools.ASPECT_CELL_SIZE_PIXEL))

  private fun PortContainer.researchArea(hexLayout: HexLayout<AspectHex>): Set<UIComponent> {

    val runes =
        Runes(
            uiOrigin = ResearchArea.bounds.origin,
            runeLimit = 16,
            hexLayout = hexLayout,
            research = research)

    return setOf(
        ParchmentUIComponent(
            research = research, uiOrigin = ResearchArea.bounds.origin, runes = runes),
        AspectHexMapUIComponent(centerUiOrigin = ResearchArea.centerOrigin, research, hexLayout),
        AspectHexMapEditorUIComponent(research, hexLayout))
  }

  private fun PortContainer.copyButton() =
      CopyButtonUIComponent(
          bounds = CopyButton.bounds,
          requirementsUiOrigin = CopyButton.requirementsUiOrigin,
          research = research,
          researcher = researcher,
          tree = tree)

  private fun PortContainer.palletComponents(
      palletGrids: List<GridLayout<Aspect>>
  ): List<UIComponent> =
      palletGrids.map { grid -> AspectPalletUIComponent(grid, pallet, researcher) }

  private fun PortContainer.palletGrids(): List<GridLayout<Aspect>> {
      val leftAspectGrid: GridLayout<Aspect>
      val rightAspectGrid: GridLayout<Aspect>
      val maxAspectsPerSide = 48
      when (ResearchTweaksConfig.client.researchTableSortingOrder) {
          AspectSortingOptions.SIMPLE_TO_COMPLEX.ordinal -> {
              leftAspectGrid = aspectGrid(bounds = AspectPools.leftBound, aspectProvider = tree::allOrderLeaning)
              rightAspectGrid = aspectGrid(bounds = AspectPools.rightBound, aspectProvider = tree::allEntropyLeaning)
          }
          AspectSortingOptions.ALPHABETICAL_FILL_LEFT.ordinal -> {
              leftAspectGrid = aspectGrid(bounds = AspectPools.leftBound, aspectProvider = { allAspects.take(maxAspectsPerSide)})
              rightAspectGrid = aspectGrid(bounds = AspectPools.rightBound, aspectProvider = { allAspects.drop(maxAspectsPerSide)})
          }
          AspectSortingOptions.ALPHABETICAL_BALANCED.ordinal -> {
              leftAspectGrid = aspectGrid(bounds = AspectPools.leftBound, aspectProvider = { allAspects.take(allAspects.size / 2)})
              rightAspectGrid = aspectGrid(bounds = AspectPools.rightBound, aspectProvider = { allAspects.drop(allAspects.size / 2)})
          }
          else -> throw Exception("unknown config " + AspectSortingOptions.ALPHABETICAL_BALANCED.ordinal)
      }
      return listOf(leftAspectGrid, rightAspectGrid)
  }

  private fun PortContainer.aspectGrid(
      bounds: Rectangle,
      aspectProvider: () -> List<Aspect>
  ): GridLayout<Aspect> =
      GridLayoutDynamicListAdapter(
          bounds = bounds,
          cellSize = AspectPools.ASPECT_CELL_SIZE_PIXEL,
      ) {
        val discoveredAspects = researcher.allDiscoveredAspects()
        aspectProvider().filter { aspect -> aspect in discoveredAspects }
      }
}
