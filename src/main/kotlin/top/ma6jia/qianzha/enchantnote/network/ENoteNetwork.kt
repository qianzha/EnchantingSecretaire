package top.ma6jia.qianzha.enchantnote.network

import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.network.NetworkRegistry
import net.minecraftforge.fml.network.PacketDistributor
import net.minecraftforge.fml.network.simple.SimpleChannel
import top.ma6jia.qianzha.enchantnote.EnchantNote
import top.ma6jia.qianzha.enchantnote.capability.IEnchantKeeper
import top.ma6jia.qianzha.enchantnote.network.message.EnchantKeeperMsg
import top.ma6jia.qianzha.enchantnote.network.message.ScannerSelectMsg

object ENoteNetwork {
    private const val VERSION = "1.0"

    val CHANNEL: SimpleChannel = NetworkRegistry.newSimpleChannel(
        ResourceLocation(EnchantNote.MODID, "note_network"),
        { VERSION },
        VERSION::equals,
        VERSION::equals
    )

    private var id = 0

    fun register() {
        CHANNEL.messageBuilder(EnchantKeeperMsg::class.java, id++)
            .decoder(EnchantKeeperMsg::decode)
            .encoder(EnchantKeeperMsg::encode)
            .consumer(EnchantKeeperMsg::onOpenNotebookScreen)
            .add()
        CHANNEL.messageBuilder(ScannerSelectMsg::class.java, id++)
            .decoder(ScannerSelectMsg::decode)
            .encoder(ScannerSelectMsg::encode)
            .consumer(ScannerSelectMsg::handle)
            .add()
    }

    fun openKeeper(
        keeper: IEnchantKeeper,
        player: ServerPlayerEntity,
        pos: BlockPos? = null
    ) {
        CHANNEL.send(
            PacketDistributor.PLAYER.with { player },
            EnchantKeeperMsg(keeper, pos)
        )
    }
}