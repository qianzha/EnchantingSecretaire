package top.ma6jia.qianzha.enchantnote.capability

import net.minecraft.client.resources.I18n
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
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

    override fun enchant(target: ItemStack, enchantment: Enchantment, level: Int): ItemStack {
        if (target.item !== Items.BOOK && !enchantment.canApply(target)) return ItemStack.EMPTY

        val request = 1u shl (level - 1)
        val levelI = getLevelI(enchantment)
        when {
            request > levelI -> return ItemStack.EMPTY
            request == levelI -> hold.remove(enchantment)
            else -> hold[enchantment] = levelI - request
        }
        val res = if(target.item === Items.BOOK)
                ItemStack(Items.ENCHANTED_BOOK, target.count)
            else
                target.copy()
        res.addEnchantment(enchantment, level)
        return res
    }

    override fun getLevelI(enchantment: Enchantment): UInt =
        hold.getOrDefault(enchantment, 0u)

    override fun numOfLevel(enchantment: Enchantment, level: Int): UInt =
        getLevelI(enchantment) shr (level - 1)

    override fun numOfEnchantments(): Int = hold.size

    override fun entities() = hold.entries

    override fun keys() = hold.keys

}