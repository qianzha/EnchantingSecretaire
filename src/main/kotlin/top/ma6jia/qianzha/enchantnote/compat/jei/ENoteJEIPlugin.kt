package top.ma6jia.qianzha.enchantnote.compat.jei

import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.client.resources.I18n
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import top.ma6jia.qianzha.enchantnote.EnchantNote
import top.ma6jia.qianzha.enchantnote.item.ENoteItems

@JeiPlugin
class ENoteJEIPlugin : IModPlugin {
    override fun getPluginUid(): ResourceLocation =
        ResourceLocation(EnchantNote.MODID, "jei_plugin")

    override fun registerRecipes(registration: IRecipeRegistration) {
        registration.addIngredientInfo(ItemStack(ENoteItems.ENCHANT_SCANNER), VanillaTypes.ITEM,
            I18n.format("info.enchantnote.enchant_scanner"))
        registration.addIngredientInfo(ItemStack(ENoteItems.ENCHANT_TABLE_CLOTH), VanillaTypes.ITEM,
            I18n.format("info.enchantnote.enchant_table_cloth"))
    }
}