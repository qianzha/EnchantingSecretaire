package top.ma6jia.qianzha.enchantnote.network

import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.network.NetworkRegistry
import top.ma6jia.qianzha.enchantnote.EnchantNote
import top.ma6jia.qianzha.enchantnote.network.message.EnchantKeeperMsg

object ENoteNetwork {
    val VERSION = "1.0"

    val CHANNEL = NetworkRegistry.newSimpleChannel(
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
    }
}