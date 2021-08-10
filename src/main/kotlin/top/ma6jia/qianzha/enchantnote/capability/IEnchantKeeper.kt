package top.ma6jia.qianzha.enchantnote.capability

import net.minecraft.client.gui.screen.ReadBookScreen
import net.minecraft.client.resources.I18n
import net.minecraft.enchantment.Enchantment
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.nbt.IntNBT
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextProperties
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.registries.ForgeRegistries

interface IEnchantKeeper {
    /**
     * @param enchantment
     * @param levelI level to multiply
     * @return level out of limit. Learn detail at [getLevel]
     */
    fun insert(enchantment: Enchantment, levelI: UInt) : UInt

    /**
     * @param enchantment
     * @return Total level as the number of Level I.
     *  As two enchantments has same level could be combine into next level,
     *  `level shl 1` is the number of Level II saved.
     */
    fun getLevelI(enchantment: Enchantment): UInt

    fun entities(): MutableSet<MutableMap.MutableEntry<Enchantment, UInt>>
    fun keys(): MutableSet<Enchantment>

    val bookInfo: ReadBookScreen.IBookInfo
        get() = object : ReadBookScreen.IBookInfo {
            val pages : List<String> = entities().map {
                """
${I18n.format(it.key.name)}:
    ${it.value} / ${UInt.MAX_VALUE}
                """.trimIndent()
            }

            override fun getPageCount(): Int = pages.size

            override fun func_230456_a_(p_230456_1_: Int): ITextProperties =
                ITextProperties.func_240652_a_(this.pages[p_230456_1_]);
        }

    object Storage : Capability.IStorage<IEnchantKeeper> {
        override fun writeNBT(
            capability: Capability<IEnchantKeeper>?,
            instance: IEnchantKeeper?,
            side: Direction?
        ): CompoundNBT {
            val nbt = CompoundNBT()
            instance!!.entities().forEach {
                nbt.put(it.key.registryName.toString(), IntNBT.valueOf(it.value.toInt()))
            }
            return nbt
        }

        override fun readNBT(
            capability: Capability<IEnchantKeeper>?,
            instance: IEnchantKeeper?,
            side: Direction?,
            nbt: INBT?
        ) {
            if (nbt is CompoundNBT) {
                nbt.keySet().forEach { keyRL ->
                    ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation(keyRL))?.let { key ->
                        instance!!.insert(key, (nbt.get(keyRL) as IntNBT).int.toUInt())
                    }
                }
            }
        }
    }

}