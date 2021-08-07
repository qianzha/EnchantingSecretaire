package top.ma6jia.qianzha.enchantnote.capability

import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.LazyOptional


class EnchantKeeperProvider : ICapabilitySerializable<CompoundNBT> {
    companion object {
        private val CAP = ENoteCapability.ENCHANT_KEEPER_CAPABILITY
        private val STORAGE = CAP.storage
    }
    private val keeper = EnchantKeeper()

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        return CAP.orEmpty(cap, LazyOptional.of { keeper })
    }

    override fun serializeNBT(): CompoundNBT {
        val nbt = CompoundNBT()
        nbt.put("enchant_keep", STORAGE.writeNBT(CAP, keeper, null)!!)
        return nbt
    }

    override fun deserializeNBT(nbt: CompoundNBT?) {
        val subTag = nbt!!.get("enchant_keep")
        STORAGE.readNBT(CAP, keeper, null, subTag)
    }
}