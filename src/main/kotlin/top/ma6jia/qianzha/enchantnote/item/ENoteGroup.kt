package top.ma6jia.qianzha.enchantnote.item

import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack

object ENoteGroup : ItemGroup("enchant_note") {
    override fun createIcon(): ItemStack = ItemStack(ENoteItems.ENCHANT_NOTE)
}