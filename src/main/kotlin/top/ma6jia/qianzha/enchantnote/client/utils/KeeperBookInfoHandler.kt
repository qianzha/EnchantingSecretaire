package top.ma6jia.qianzha.enchantnote.client.utils

import net.minecraft.client.gui.screen.ReadBookScreen
import net.minecraft.client.resources.I18n
import net.minecraft.enchantment.Enchantment
import net.minecraft.util.text.ITextProperties
import net.minecraft.util.text.TextComponent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import top.ma6jia.qianzha.enchantnote.capability.IEnchantKeeper

@OnlyIn(Dist.CLIENT)
class KeeperBookInfoHandler(private val keeper: IEnchantKeeper): ReadBookScreen.IBookInfo {

    override fun getPageCount(): Int = keeper.numOfEnchantments()

    var currentEcm : Enchantment? = null
        private set

    override fun func_230456_a_(p_230456_1_: Int): ITextProperties {
        return keeper.entities().elementAtOrNull(p_230456_1_)?.let { (ecm, levelI) ->
            currentEcm = ecm
            // TODO i18n
            val maxLevel = I18n.format("enchantment.level.${ecm.maxLevel}")
            val page = (ecm.getDisplayName(ecm.minLevel) as TextComponent)
            for(i in (ecm.maxLevel - 1) downTo (ecm.minLevel - 1)) {
                val levelOfi = levelI.toDouble() / (1 shl i)
                if(levelOfi >= 1) {
                    val iLevel = I18n.format("enchantment.level.${i + 1}")
                    page.appendString(":\n $levelOfi of Level $iLevel")
                }
            }
            page.appendString("\n  Limit: ${UInt.MAX_VALUE shr (ecm.maxLevel - 1)} of Level $maxLevel")
        } ?: ITextProperties.field_240651_c_
    }
}