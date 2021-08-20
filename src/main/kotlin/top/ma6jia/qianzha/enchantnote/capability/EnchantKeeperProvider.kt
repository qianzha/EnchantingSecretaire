package top.ma6jia.qianzha.enchantnote.capability

import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.LazyOptional


class EnchantKeeperProvider(stack: ItemStack) : ICapabilitySerializable<CompoundNBT> {
    companion object {
        private val CAP
            get() = ENoteCapability.ENCHANT_KEEPER_CAPABILITY
        private val STORAGE
            get() = CAP.storage
    }
    private val keeper = EnchantKeeper()

    init {
        stack.tag?.let {
            deserializeNBT(it)
        }
    }

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        return CAP.orEmpty(cap, LazyOptional.of { keeper })
    }

    override fun serializeNBT(): CompoundNBT {
        val nbt = CompoundNBT()
        nbt.put("enchant_keep", STORAGE.writeNBT(CAP, keeper, null)!!)
        return nbt
    }

    override fun deserializeNBT(nbt: CompoundNBT?) {
        val subTag = nbt!!.getCompound("enchant_keep")
        STORAGE.readNBT(CAP, keeper, null, subTag)
    }
}