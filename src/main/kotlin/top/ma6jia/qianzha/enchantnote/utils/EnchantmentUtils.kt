package top.ma6jia.qianzha.enchantnote.utils

import net.minecraft.enchantment.Enchantment

object EnchantmentUtils {
    fun getMultiplier(ench: Enchantment) =
        when (ench.rarity) {
            Enchantment.Rarity.VERY_RARE -> 8
            Enchantment.Rarity.RARE -> 4
            Enchantment.Rarity.UNCOMMON -> 2
            else -> 1
        }
}