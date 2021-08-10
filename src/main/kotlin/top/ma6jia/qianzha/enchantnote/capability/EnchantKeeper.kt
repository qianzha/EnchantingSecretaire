package top.ma6jia.qianzha.enchantnote.capability

import net.minecraft.client.resources.I18n
import net.minecraft.enchantment.Enchantment
import net.minecraft.util.text.ITextProperties
import net.minecraft.util.text.TextComponent

class EnchantKeeper : IEnchantKeeper {
    private val hold = linkedMapOf<Enchantment, UInt>()

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

    override fun getPageCount(): Int = hold.size

    override fun func_230456_a_(p_230456_1_: Int): ITextProperties {
        return hold.entries.elementAtOrNull(p_230456_1_)?.let { (ecm, levelI) ->
            // TODO i18n
            val maxLevel = I18n.format("enchantment.level.${ecm.maxLevel}")
            val page = (ecm.getDisplayName(ecm.minLevel) as TextComponent)
            for(i in (ecm.maxLevel - 1) downTo (ecm.minLevel - 1)) {
                val levelOfi = levelI.toDouble() / (1 shl i)
                if(levelOfi > 1) {
                    val iLevel = I18n.format("enchantment.level.${i + 1}")
                    page.appendString(":\n $levelOfi of Level $iLevel")
                    break
                }
            }
            page.appendString("\n  Limit: ${UInt.MAX_VALUE shr (ecm.maxLevel - 1)} of Level $maxLevel")
        } ?: ITextProperties.field_240651_c_
    }
}