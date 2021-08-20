package top.ma6jia.qianzha.enchantnote.client

import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import top.ma6jia.qianzha.enchantnote.block.ENoteBlocks
import top.ma6jia.qianzha.enchantnote.capability.ENoteCapability
import top.ma6jia.qianzha.enchantnote.client.renderer.EnchantScannerTER
import top.ma6jia.qianzha.enchantnote.tileentity.ENoteTileEntities


object ClientHandler {
    fun register() {
        MOD_BUS.addListener(::onRenderTypeSetup)
        FORGE_BUS.addListener(this::onTooltip)
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
                    "tooltip.enchantnote.keeper.num", it.numOfEnchantments())
                )
            }
    }
}