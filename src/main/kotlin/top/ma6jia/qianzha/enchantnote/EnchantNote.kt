package top.ma6jia.qianzha.enchantnote

import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runWhenOn
import top.ma6jia.qianzha.enchantnote.block.ENoteBlocks
import top.ma6jia.qianzha.enchantnote.capability.EnchantKeeper
import top.ma6jia.qianzha.enchantnote.capability.EnchantKeeperProvider
import top.ma6jia.qianzha.enchantnote.capability.IEnchantKeeper
import top.ma6jia.qianzha.enchantnote.client.ClientHandler
import top.ma6jia.qianzha.enchantnote.item.ENoteItems
import top.ma6jia.qianzha.enchantnote.tileentity.ENoteTileEntities

@Mod(EnchantNote.MODID)
object EnchantNote {
    const val MODID = "enchantnote"
    val LOGGER: Logger = LogManager.getLogger()

    init {
        LOGGER.info("Hello Init")

        MOD_BUS.addListener(EnchantNote::onSetUpEvent)
        FORGE_BUS.addListener(EnchantNote::attachCap)

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
        }
    }

    private fun attachCap(event: AttachCapabilitiesEvent<ItemStack>){
        val stack : Any = event.`object`
        if(stack is ItemStack && stack.item === ENoteItems.ENCHANT_NOTE) {
            event.addCapability(
                ResourceLocation(MODID, "enchant_keeper"),
                EnchantKeeperProvider()
            )
        }
    }
}