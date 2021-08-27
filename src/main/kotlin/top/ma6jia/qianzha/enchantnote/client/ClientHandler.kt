package top.ma6jia.qianzha.enchantnote.client

import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.client.event.DrawHighlightEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import top.ma6jia.qianzha.enchantnote.block.ENoteBlocks
import top.ma6jia.qianzha.enchantnote.capability.ENoteCapability
import top.ma6jia.qianzha.enchantnote.client.gui.ScannerInfoHUD
import top.ma6jia.qianzha.enchantnote.client.renderer.EnchantScannerTER
import top.ma6jia.qianzha.enchantnote.tileentity.ENoteTileEntities


object ClientHandler {
    fun register() {
        MOD_BUS.addListener(this::onRenderTypeSetup)
        FORGE_BUS.addListener(this::onTooltip)
        FORGE_BUS.addListener(this::onDrawHighlight)
        FORGE_BUS.addListener(this::onRenderOverlay)
    }

    private fun onRenderTypeSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            RenderTypeLookup.setRenderLayer(ENoteBlocks.ENCHANT_SCANNER, RenderType.getCutout())
            ClientRegistry.bindTileEntityRenderer(ENoteTileEntities.ENCHANT_SCANNER, ::EnchantScannerTER)
        }
    }

    private fun onTooltip(event: ItemTooltipEvent) {
        event.itemStack.getCapability(ENoteCapability.ENCHANT_KEEPER_CAPABILITY)
            .ifPresent {
                event.toolTip.add(
                    TranslationTextComponent(
                        "tooltip.enchantnote.keeper.num", it.numOfEnchantments()
                    )
                )
            }
    }

    private fun onRenderOverlay(event: RenderGameOverlayEvent) {
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT)
            ScannerInfoHUD.render(event.matrixStack)
    }

    private fun onDrawHighlight(event: DrawHighlightEvent) {
        val target = event.target
        ScannerInfoHUD.pos = if (target is BlockRayTraceResult)
            target.pos else null
    }
}