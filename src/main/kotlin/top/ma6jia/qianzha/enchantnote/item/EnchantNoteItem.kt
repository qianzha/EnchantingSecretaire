package top.ma6jia.qianzha.enchantnote.item

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.world.World
import net.minecraftforge.fml.network.PacketDistributor
import top.ma6jia.qianzha.enchantnote.capability.ENoteCapability
import top.ma6jia.qianzha.enchantnote.network.ENoteNetwork
import top.ma6jia.qianzha.enchantnote.network.message.EnchantKeeperMsg
import top.ma6jia.qianzha.enchantnote.utils.Log4j
import top.ma6jia.qianzha.enchantnote.utils.Log4j.Companion.log

@Log4j
class EnchantNoteItem(properties: Properties) : Item(properties) {

    override fun onItemRightClick(worldIn: World, playerIn: PlayerEntity, handIn: Hand): ActionResult<ItemStack> {
        if(playerIn is ServerPlayerEntity) {
            log.debug("Server Right Click")
            playerIn.getHeldItem(handIn)
                .getCapability(ENoteCapability.ENCHANT_KEEPER_CAPABILITY)
                .ifPresent {
                    log.debug("Keeper Right Click")
                    ENoteNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with { playerIn },
                        EnchantKeeperMsg(it)
                )
            }
            val off = playerIn.heldItemOffhand
            if(off.item === Items.WRITTEN_BOOK) {
                log.debug("{}", off.tag)
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn)
    }
}