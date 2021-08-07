package top.ma6jia.qianzha.enchantnote.client

import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import top.ma6jia.qianzha.enchantnote.block.ENoteBlocks
import top.ma6jia.qianzha.enchantnote.client.renderer.EnchantScannerTER
import top.ma6jia.qianzha.enchantnote.tileentity.ENoteTileEntities

object ClientHandler {
    fun register() {
        MOD_BUS.addListener(::onRenderTypeSetup)
    }

    private fun onRenderTypeSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            RenderTypeLookup.setRenderLayer(ENoteBlocks.ENCHANT_SCANNER, RenderType.getCutout())
            ClientRegistry.bindTileEntityRenderer(ENoteTileEntities.ENCHANT_SCANNER, ::EnchantScannerTER)
        }
    }

}