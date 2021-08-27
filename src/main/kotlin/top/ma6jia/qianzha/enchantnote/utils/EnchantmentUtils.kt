package top.ma6jia.qianzha.enchantnote.utils

import net.minecraft.enchantment.Enchantment
import top.ma6jia.qianzha.enchantnote.config.ENoteCommonConfig

object EnchantmentUtils {
    @JvmStatic
    fun getMultiplier(ench: Enchantment) =
        when (ench.rarity) {
            Enchantment.Rarity.VERY_RARE -> 8
            Enchantment.Rarity.RARE -> 4
            Enchantment.Rarity.UNCOMMON -> 2
            else -> 1
        }

    @JvmStatic
    fun isCostLimit(cost: Int): Boolean =
        cost >= ENoteCommonConfig.ENCHANT_COST_LIMIT.get()
}