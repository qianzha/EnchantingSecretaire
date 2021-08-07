package top.ma6jia.qianzha.enchantnote.tileentity

import net.minecraft.tileentity.TileEntityType
import net.minecraftforge.registries.ForgeRegistries.TILE_ENTITIES
import thedarkcolour.kotlinforforge.forge.KDeferredRegister
import top.ma6jia.qianzha.enchantnote.EnchantNote.MODID
import top.ma6jia.qianzha.enchantnote.block.ENoteBlocks

object ENoteTileEntities {
    val REGISTRY = KDeferredRegister(TILE_ENTITIES, MODID)

    val ENCHANT_SCANNER: TileEntityType<EnchantScannerTE> by REGISTRY.registerObject("enchant_scanner") {
        TileEntityType.Builder.create(::EnchantScannerTE, ENoteBlocks.ENCHANT_SCANNER).build(null)
    }
}