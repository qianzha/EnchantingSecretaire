package top.ma6jia.qianzha.enchantnote.item

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.world.World
import top.ma6jia.qianzha.enchantnote.capability.ENoteCapability
import top.ma6jia.qianzha.enchantnote.network.ENoteNetwork

class EnchantNoteItem(properties: Properties) : Item(properties) {

    override fun onItemRightClick(worldIn: World, playerIn: PlayerEntity, handIn: Hand): ActionResult<ItemStack> {
        if(playerIn is ServerPlayerEntity) {
            playerIn.getHeldItem(handIn)
                .getCapability(ENoteCapability.ENCHANT_KEEPER_CAPABILITY)
                .ifPresent {
                    ENoteNetwork.openKeeper(it, playerIn)
                }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn)
    }
}