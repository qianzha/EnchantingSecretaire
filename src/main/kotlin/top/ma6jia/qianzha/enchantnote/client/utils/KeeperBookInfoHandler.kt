package top.ma6jia.qianzha.enchantnote.client.utils

import net.minecraft.client.gui.screen.ReadBookScreen
import net.minecraft.client.resources.I18n
import net.minecraft.enchantment.Enchantment
import net.minecraft.util.text.ITextProperties
import net.minecraft.util.text.TextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import top.ma6jia.qianzha.enchantnote.capability.IEnchantKeeper
import top.ma6jia.qianzha.enchantnote.utils.maxLvl
import top.ma6jia.qianzha.enchantnote.utils.minLvl
import top.ma6jia.qianzha.enchantnote.utils.EnchantmentUtils as Utils

@OnlyIn(Dist.CLIENT)
class KeeperBookInfoHandler(private val keeper: IEnchantKeeper) : ReadBookScreen.IBookInfo {

    override fun getPageCount(): Int = keeper.numOfEnchantments()

    var currentEcm: Enchantment? = null
        private set

    override fun func_230456_a_(p_230456_1_: Int): ITextProperties {
        return keeper.entities().elementAtOrNull(p_230456_1_)?.let { (ecm, levelI) ->
            currentEcm = ecm
            // TODO i18n
            val maxLevel = I18n.format("enchantment.level.${ecm.maxLvl()}")
            val page = (ecm.getDisplayName(ecm.minLvl()) as TextComponent)
            for (i in (ecm.maxLvl() - 1) downTo (ecm.minLvl() - 1)) {
                val levelOfi = levelI.toDouble() / (1 shl i)
                if (levelOfi >= 1) {
                    val iLevel = I18n.format("enchantment.level.${i + 1}")
                    page.appendSibling(
                        TranslationTextComponent(
                            "gui.enchantnote.keeper.level_num", levelOfi, iLevel
                        )
                    )
                }
            }
            page.appendSibling(
                TranslationTextComponent(
                    "gui.enchantnote.keeper.max_limit",
                    UInt.MAX_VALUE shr (ecm.maxLvl() - 1), maxLevel
                )
            )
        } ?: ITextProperties.field_240651_c_
    }
}