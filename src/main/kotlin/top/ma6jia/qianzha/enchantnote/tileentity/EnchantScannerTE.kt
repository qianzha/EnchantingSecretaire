package top.ma6jia.qianzha.enchantnote.tileentity

import net.minecraft.block.BlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
import net.minecraftforge.items.ItemHandlerHelper
import net.minecraftforge.items.ItemStackHandler
import top.ma6jia.qianzha.enchantnote.block.EnchantScannerBlock
import top.ma6jia.qianzha.enchantnote.capability.ENoteCapability.ENCHANT_KEEPER_CAPABILITY

class EnchantScannerTE : TileEntity(ENoteTileEntities.ENCHANT_SCANNER) {
    private val keeperStackHandler = object : ItemStackHandler(1) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean =
            stack.getCapability(ENCHANT_KEEPER_CAPABILITY).isPresent

        override fun onContentsChanged(slot: Int) {
            super.onContentsChanged(slot)
            checkHasKeeper()
        }
    }

    private val bookshelfHandler = object : ItemStackHandler(1) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean =
            stack.item === Items.BOOK

        override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
            // 只接受附魔书，当 keeperStack 为空时拒绝接收
            val note = keeperStackHandler.getStackInSlot(0)
            val keeperOpt = note.getCapability(ENCHANT_KEEPER_CAPABILITY)
            if (note.isEmpty || !keeperOpt.isPresent || stack.item !== Items.ENCHANTED_BOOK)
                return stack

            // 将附魔书转为普通书交由超类存放，非 simulate 则将存放成功数量的附魔书信息存入 enchant_keeper，等级超过存储上限直接丢弃
            val bookStack = ItemStack(Items.BOOK, stack.count)
            val bookInsertResult = super.insertItem(slot, bookStack, simulate)
            val num = (stack.count - bookInsertResult.count).toUInt()
            if (!simulate && num != 0u) {
                val maxLevelI = UInt.MAX_VALUE / num
                keeperOpt.ifPresent { keeper ->
                    EnchantmentHelper.getEnchantments(stack).forEach { (enchantment, level) ->
                        val levelI = 1u shl level
                        keeper.insert(enchantment, if (levelI > maxLevelI) UInt.MAX_VALUE else levelI * num)
                    }
                }
            }

            return if (bookInsertResult.isEmpty) ItemStack.EMPTY else ItemHandlerHelper.copyStackWithSize(
                stack,
                bookInsertResult.count
            )
        }

        override fun onContentsChanged(slot: Int) {
            super.onContentsChanged(slot)
            checkBookshelfInv()
        }
    }
    val inventory = listOf(keeperStackHandler, bookshelfHandler)

    fun checkHasKeeper() {
        val newState = blockState.with(
            EnchantScannerBlock.HAS_KEEPER,
            !keeperStackHandler.getStackInSlot(0).isEmpty
        )
        world!!.setBlockState(pos, newState)
    }

    fun checkBookshelfInv() {
        val newState = blockState.with(
            EnchantScannerBlock.BOOKSHELF_INV,
            minOf(bookshelfHandler.getStackInSlot(0).stack.count / 10, 6)
        )
        world!!.setBlockState(pos, newState)
    }

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?)
            : LazyOptional<T> = if (side === Direction.UP) {
        ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(this::keeperStackHandler))
    } else {
        ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(this::bookshelfHandler))
    }


    override fun read(state: BlockState, nbt: CompoundNBT) {
        super.read(state, nbt)
        keeperStackHandler.deserializeNBT(nbt.getCompound("keeper"))
        bookshelfHandler.deserializeNBT(nbt.getCompound("books"))
    }

    override fun write(compound: CompoundNBT): CompoundNBT {
        compound.put("keeper", keeperStackHandler.serializeNBT())
        compound.put("books", bookshelfHandler.serializeNBT())
        return super.write(compound)
    }
}