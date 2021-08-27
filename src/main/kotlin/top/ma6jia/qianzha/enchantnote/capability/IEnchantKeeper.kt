package top.ma6jia.qianzha.enchantnote.capability

import net.minecraft.enchantment.Enchantment
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.nbt.IntNBT
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.registries.ForgeRegistries

interface IEnchantKeeper {
    /**
     * @param enchantment
     * @param levelI level to multiply
     * @return level out of limit. Learn detail at [getLevelI]
     */
    fun insert(enchantment: Enchantment, levelI: UInt): UInt

    operator fun set(enchantment: Enchantment, levelI: UInt)

    /**
     * @see getLevelI
     */
    operator fun get(enchantment: Enchantment): UInt = getLevelI(enchantment)

    fun remove(enchantment: Enchantment)

    /**
     * @param enchantment
     * @return Total level as the number of Level I.
     *  As two enchantments has same level could be combine into next level,
     *  `levelI shr 1` is the number of Level II saved.
     *
     *  Meanwhile, Level II request 2 of Level I, III request 4, IV request 8, and so on.
     */
    fun getLevelI(enchantment: Enchantment): UInt

    fun numOfLevel(enchantment: Enchantment, level: Int): UInt
    fun numOfEnchantments(): Int

    fun entities(): MutableSet<MutableMap.MutableEntry<Enchantment, UInt>>
    fun keys(): MutableSet<Enchantment>

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