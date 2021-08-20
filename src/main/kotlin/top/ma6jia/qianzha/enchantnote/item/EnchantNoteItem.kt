package top.ma6jia.qianzha.enchantnote.item

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.ICapabilityProvider
import top.ma6jia.qianzha.enchantnote.capability.ENoteCapability.ENCHANT_KEEPER_CAPABILITY
import top.ma6jia.qianzha.enchantnote.capability.EnchantKeeperProvider
import top.ma6jia.qianzha.enchantnote.capability.IEnchantKeeper
import top.ma6jia.qianzha.enchantnote.network.ENoteNetwork

class EnchantNoteItem(properties: Properties) : Item(properties) {

    override fun onItemRightClick(worldIn: World, playerIn: PlayerEntity, handIn: Hand): ActionResult<ItemStack> {
        if (playerIn is ServerPlayerEntity) {
            playerIn.getHeldItem(handIn)
                .getCapability(ENCHANT_KEEPER_CAPABILITY)
                .ifPresent {
                    ENoteNetwork.openKeeper(it, playerIn)
                }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn)
    }

    override fun initCapabilities(stack: ItemStack, nbt: CompoundNBT?)
            : ICapabilityProvider = EnchantKeeperProvider(stack)

    override fun getShareTag(stack: ItemStack): CompoundNBT? {
        stack.getCapability(ENCHANT_KEEPER_CAPABILITY)
            .ifPresent {
                stack.setTagInfo("enchant_keep",
                    IEnchantKeeper.Storage.writeNBT(
                        ENCHANT_KEEPER_CAPABILITY,
                        it, null
                    ))
            }
        return super.getShareTag(stack)
    }

    override fun readShareTag(stack: ItemStack?, nbt: CompoundNBT?) {
        super.readShareTag(stack, nbt)
        stack!!.getCapability(ENCHANT_KEEPER_CAPABILITY).ifPresent {
            ENCHANT_KEEPER_CAPABILITY.storage.readNBT(
                ENCHANT_KEEPER_CAPABILITY, it, null, nbt?.getCompound("enchant_keep")
            )
        }
    }
}