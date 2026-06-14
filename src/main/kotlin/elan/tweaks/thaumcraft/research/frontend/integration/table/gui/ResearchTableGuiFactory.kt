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
        gui(
            scale = ResearchTableLayout.guiScale,
            container = inventory,
            components =
                tableAndInventoryBackgrounds() +
                    researchArea() +
                    copyButton() +
                    palletComponents() +
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

  private fun PortContainer.researchArea(): Set<UIComponent> {

    val hexLayout: HexLayout<AspectHex> =
        HexLayoutResearchNoteDataAdapter(
            bounds = ResearchArea.bounds,
            centerUiOrigin = ResearchArea.centerOrigin,
            hexSize = HexTexture.SIZE_PIXELS,
            aspectTree = tree,
            researcher = researcher,
            researchProcess = research)

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

  private fun PortContainer.palletComponents(): List<UIComponent> {
      var leftAspectPallet: AspectPalletUIComponent
      var rightAspectPallet: AspectPalletUIComponent
      val maxAspectsPerSide = 48
      when (ResearchTweaksConfig.client.researchTableSortingOrder) {
          AspectSortingOptions.SIMPLE_TO_COMPLEX.ordinal -> {
              leftAspectPallet = palletComponent(bounds = AspectPools.leftBound, aspectProvider = tree::allOrderLeaning)
              rightAspectPallet = palletComponent(bounds = AspectPools.rightBound, aspectProvider = tree::allEntropyLeaning)
          }
          AspectSortingOptions.ALPHABETICAL_FILL_LEFT.ordinal -> {
              leftAspectPallet = palletComponent(bounds = AspectPools.leftBound, aspectProvider = { allAspects.take(maxAspectsPerSide)})
              rightAspectPallet = palletComponent(bounds = AspectPools.rightBound, aspectProvider = { allAspects.drop(maxAspectsPerSide)})
          }
          AspectSortingOptions.ALPHABETICAL_BALANCED.ordinal -> {
              leftAspectPallet = palletComponent(bounds = AspectPools.leftBound, aspectProvider = { allAspects.take(allAspects.size / 2)})
              rightAspectPallet = palletComponent(bounds = AspectPools.rightBound, aspectProvider = { allAspects.drop(allAspects.size / 2)})
          }
          else -> throw Exception("unknown config " + AspectSortingOptions.ALPHABETICAL_BALANCED.ordinal)
      }
      return listOf(leftAspectPallet, rightAspectPallet)
  }

  private fun PortContainer.palletComponent(
      bounds: Rectangle,
      aspectProvider: () -> List<Aspect>
  ): AspectPalletUIComponent {
    val aspectPalletGrid: GridLayout<Aspect> =
        GridLayoutDynamicListAdapter(
            bounds = bounds,
            cellSize = AspectPools.ASPECT_CELL_SIZE_PIXEL,
        ) {
          val discoveredAspects = researcher.allDiscoveredAspects()
          aspectProvider().filter { aspect -> aspect in discoveredAspects }
        }

    return AspectPalletUIComponent(aspectPalletGrid, pallet, researcher)
  }
}
