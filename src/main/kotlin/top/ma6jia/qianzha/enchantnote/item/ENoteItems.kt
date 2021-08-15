package top.ma6jia.qianzha.enchantnote.item

import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraftforge.registries.ForgeRegistries.ITEMS
import thedarkcolour.kotlinforforge.forge.KDeferredRegister
import top.ma6jia.qianzha.enchantnote.EnchantNote.MODID
import top.ma6jia.qianzha.enchantnote.block.ENoteBlocks

object ENoteItems {
    val REGISTRY = KDeferredRegister(ITEMS, MODID)

    val ENCHANT_NOTE by REGISTRY.registerObject("enchant_note") {
        EnchantNoteItem(Item.Properties().maxStackSize(1).group(ENoteGroup))
    }

    val ENCHANT_TABLE_CLOTH by REGISTRY.registerObject("enchant_table_cloth") {
        Item(Item.Properties().group(ENoteGroup))
    }

    val ENCHANT_SCANNER by REGISTRY.registerObject("enchant_scanner") {
        BlockItem(ENoteBlocks.ENCHANT_SCANNER, Item.Properties().group(ENoteGroup))
    }


}