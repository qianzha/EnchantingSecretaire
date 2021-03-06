package top.ma6jia.qianzha.enchantnote.block

import net.minecraftforge.registries.ForgeRegistries.BLOCKS
import thedarkcolour.kotlinforforge.forge.KDeferredRegister
import top.ma6jia.qianzha.enchantnote.EnchantNote

object ENoteBlocks {
    val REGISTRY = KDeferredRegister(BLOCKS, EnchantNote.MODID)

    val ENCHANT_SCANNER by REGISTRY.registerObject("enchant_scanner") {
        EnchantScannerBlock()
    }
}