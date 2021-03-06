package top.ma6jia.qianzha.enchantnote

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runWhenOn
import top.ma6jia.qianzha.enchantnote.block.ENoteBlocks
import top.ma6jia.qianzha.enchantnote.capability.EnchantKeeper
import top.ma6jia.qianzha.enchantnote.capability.IEnchantKeeper
import top.ma6jia.qianzha.enchantnote.client.ClientHandler
import top.ma6jia.qianzha.enchantnote.config.ENoteCommonConfig
import top.ma6jia.qianzha.enchantnote.item.ENoteItems
import top.ma6jia.qianzha.enchantnote.network.ENoteNetwork
import top.ma6jia.qianzha.enchantnote.tileentity.ENoteTileEntities
import top.ma6jia.qianzha.enchantnote.utils.Log4j
import top.ma6jia.qianzha.enchantnote.utils.Log4j.Companion.log

@Mod(EnchantNote.MODID)
@Log4j
object EnchantNote {
    const val MODID = "enchantnote"

    init {
        log.info("Hello Init")

        val mlContent = ModLoadingContext.get()
        mlContent.registerConfig(ModConfig.Type.COMMON, ENoteCommonConfig.COMMON_CONFIG)

        MOD_BUS.addListener(this::onSetUpEvent)
        FORGE_BUS.addListener(this::onBlockBreak)

        ENoteBlocks.REGISTRY.register(MOD_BUS)
        ENoteItems.REGISTRY.register(MOD_BUS)
        ENoteTileEntities.REGISTRY.register(MOD_BUS)

        runWhenOn(Dist.CLIENT, ClientHandler::register)
    }

    private fun onSetUpEvent(event: FMLCommonSetupEvent) {
        event.enqueueWork {
            CapabilityManager.INSTANCE.register(
                IEnchantKeeper::class.java,
                IEnchantKeeper.Storage,
                ::EnchantKeeper
            )
            ENoteNetwork.register()
        }
    }

    private fun onBlockBreak(event: BlockEvent.BreakEvent) {
        if (event.player.isCreative && event.player.isSneaking && event.state.block === ENoteBlocks.ENCHANT_SCANNER) {
            event.isCanceled = true
            event.state.onBlockClicked(event.player.world, event.pos, event.player)
        }
    }

}