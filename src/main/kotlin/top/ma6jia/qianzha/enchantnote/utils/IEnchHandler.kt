package top.ma6jia.qianzha.enchantnote.utils

import net.minecraft.enchantment.Enchantment

interface IEnchHandler {
    companion object {
        private var INSTANCE : IEnchHandler? = null
        fun getInstance() : IEnchHandler {
            INSTANCE = INSTANCE ?: try {
                val map = (Class.forName("shadows.apotheosis.ench.EnchModule")
                    .getDeclaredField("ENCHANTMENT_INFO")[null] as Map<Enchantment, Any>)
                    .mapValues { (_, v) ->
                        Pair(
                            v::class.java.getDeclaredMethod("getMinLevel").invoke(v) as Int,
                            v::class.java.getDeclaredMethod("getMaxLevel").invoke(v) as Int
                        )
                    }
                object : IEnchHandler {
                    override fun getMinLvl(ench: Enchantment): Int
                        = map[ench]?.first ?: super.getMinLvl(ench)

                    override fun getMaxLvl(ench: Enchantment): Int
                        = map[ench]?.second ?: super.getMaxLvl(ench)
                }
            } catch (e: ClassNotFoundException) {
                object : IEnchHandler {}
            }
            return INSTANCE!!
        }
    }
    fun getMinLvl(ench: Enchantment): Int = ench.minLevel

    fun getMaxLvl(ench: Enchantment): Int = ench.maxLevel
}