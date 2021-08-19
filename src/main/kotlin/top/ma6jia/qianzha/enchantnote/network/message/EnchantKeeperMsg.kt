package top.ma6jia.qianzha.enchantnote.network.message

import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.network.NetworkEvent
import top.ma6jia.qianzha.enchantnote.capability.ENoteCapability.ENCHANT_KEEPER_CAPABILITY
import top.ma6jia.qianzha.enchantnote.capability.EnchantKeeper
import top.ma6jia.qianzha.enchantnote.capability.IEnchantKeeper
import top.ma6jia.qianzha.enchantnote.client.gui.ScannerNotebookScreen
import top.ma6jia.qianzha.enchantnote.client.utils.KeeperBookInfoHandler
import java.util.concurrent.Callable
import java.util.function.Supplier

data class EnchantKeeperMsg(
    val keeper: IEnchantKeeper,
    val pos: BlockPos? = null
) {
    companion object {
        fun decode(buf: PacketBuffer) : EnchantKeeperMsg {
            val nbt = buf.readCompoundTag()!!
            val keeper = EnchantKeeper()
            IEnchantKeeper.Storage.readNBT(ENCHANT_KEEPER_CAPABILITY, keeper, null, nbt)
            val pos = if(buf.isReadable) {
                buf.readBlockPos()
            } else null
            return EnchantKeeperMsg(keeper, pos)
        }
    }

    fun encode(buf: PacketBuffer) {
        buf.writeCompoundTag(
            IEnchantKeeper.Storage
                .writeNBT(ENCHANT_KEEPER_CAPABILITY, this.keeper, null)
        )
        pos?.let { buf.writeBlockPos(it) }
    }

    fun onOpenNotebookScreen(ctx: Supplier<NetworkEvent.Context>) {
        ctx.get().enqueueWork {
            DistExecutor.unsafeCallWhenOn(Dist.CLIENT) {
                Callable {
                    Minecraft.getInstance().displayGuiScreen(
                        ScannerNotebookScreen(KeeperBookInfoHandler(keeper), pos)
                    )
                }
            }
        }
        ctx.get().packetHandled = true
    }
}