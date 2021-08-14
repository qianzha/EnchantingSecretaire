package top.ma6jia.qianzha.enchantnote.network.message

import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.fml.network.NetworkEvent
import top.ma6jia.qianzha.enchantnote.tileentity.EnchantScannerTE
import java.util.function.Supplier

data class ScannerSelectMsg(
    val pos: BlockPos,
    val enchantment: String
) {
    companion object {
        fun decode(buf: PacketBuffer) : ScannerSelectMsg =
            ScannerSelectMsg(
                buf.readBlockPos(),
                buf.readString()
            )
    }
    fun encode(buf: PacketBuffer) {
        buf.writeBlockPos(pos)
        buf.writeString(enchantment)
    }

        fun handle(ctx: Supplier<NetworkEvent.Context>) {
        ctx.get().let {
            it.enqueueWork {
                val world = it.sender?.world
                if(world is ServerWorld) {
                    if(!world.chunkProvider.isChunkLoaded(ChunkPos(pos)))
                        return@enqueueWork
                    val tile = world.getTileEntity(pos)
                    if(tile is EnchantScannerTE) {
                        tile.setSelected(enchantment)
                    }
                }
            }
        }
        ctx.get().packetHandled = true
    }
}