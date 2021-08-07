package top.ma6jia.qianzha.enchantnote.capability

import net.minecraft.enchantment.Enchantment

class EnchantKeeper : IEnchantKeeper {
    private val hold = hashMapOf<Enchantment, UInt>()

    override fun insert(enchantment: Enchantment, levelI: UInt): UInt {
        val before = getLevelI(enchantment)
        val limit = UInt.MAX_VALUE - before
        return if (limit > levelI) {
            hold[enchantment] = before + levelI
            0u
        } else {
            hold[enchantment] = UInt.MAX_VALUE
            levelI - limit
        }
    }

    override fun getLevelI(enchantment: Enchantment): UInt =
        hold.getOrDefault(enchantment, 0u)

    override fun entities() = hold.entries

    override fun keys() = hold.keys

}