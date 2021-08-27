package top.ma6jia.qianzha.enchantnote.tileentity

import net.minecraft.block.BlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.inventory.container.RepairContainer
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SUpdateTileEntityPacket
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
import net.minecraftforge.items.ItemStackHandler
import net.minecraftforge.registries.ForgeRegistries
import top.ma6jia.qianzha.enchantnote.block.EnchantScannerBlock
import top.ma6jia.qianzha.enchantnote.capability.ENoteCapability.ENCHANT_KEEPER_CAPABILITY
import top.ma6jia.qianzha.enchantnote.capability.IEnchantKeeper
import top.ma6jia.qianzha.enchantnote.config.ENoteCommonConfig
import top.ma6jia.qianzha.enchantnote.network.ENoteNetwork
import top.ma6jia.qianzha.enchantnote.utils.EnchantmentUtils
import kotlin.math.ceil

class EnchantScannerTE : TileEntity(ENoteTileEntities.ENCHANT_SCANNER) {
    private val keeperStackHandler = object : ItemStackHandler(1) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean =
            stack.getCapability(ENCHANT_KEEPER_CAPABILITY).isPresent

        override fun onContentsChanged(slot: Int) {
            super.onContentsChanged(slot)
            checkHasKeeper()
        }
    }

    private val bookshelfHandler = object : ItemStackHandler(2) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean =
            slot == 0 && stack.item === Items.ENCHANTED_BOOK

        /**
         * 0号槽中继放附魔书，1号槽转出普通书
         * 原本不中继直接变书，因原版漏斗逻辑，有普通书时，附魔书无法塞入
         */
        override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
            if (slot != 0) return stack
            // 没有 keeper 拒收，普通书槽满拒收
            val note = keeperStackHandler.getStackInSlot(0)
            val keeperOpt = note.getCapability(ENCHANT_KEEPER_CAPABILITY)
            val transStack = getStackInSlot(1)
            if (note.isEmpty || !keeperOpt.isPresent || transStack.count >= transStack.maxStackSize)
                return stack
            return super.insertItem(slot, stack, simulate)
        }

        private fun tryGetEnchantedBook() {
            val enchantedBook = getStackInSlot(0)
            if (enchantedBook.isEmpty) return

            // 将附魔书转为普通书，将存放成功数量的附魔书信息存入 enchant_keeper，等级超过存储上限直接丢弃
            val normalBook = getStackInSlot(1)
            if (normalBook.isEmpty) {
                stacks[1] = ItemStack(Items.BOOK, enchantedBook.count)
            } else {
                stacks[1].grow(enchantedBook.count)
            }
            stacks[0] = ItemStack.EMPTY

            val num = enchantedBook.count.toUInt()
            val maxLevelI = UInt.MAX_VALUE / num
            keeperStackHandler.getStackInSlot(0)
                .getCapability(ENCHANT_KEEPER_CAPABILITY)
                .ifPresent { keeper ->
                    EnchantmentHelper.getEnchantments(enchantedBook).forEach { (enchantment, level) ->
                        val levelI = 1u shl (level - 1)
                        keeper.insert(enchantment, if (levelI > maxLevelI) UInt.MAX_VALUE else levelI * num)
                    }
                }
        }

        override fun onContentsChanged(slot: Int) {
            if (slot == 0) tryGetEnchantedBook()
            super.onContentsChanged(slot)
            checkBookshelfInv()
        }
    }

    private val enchantable = object : ItemStackHandler(1) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
            return stack.isEnchantable || stack.isEnchanted
        }

        override fun onContentsChanged(slot: Int) {
            super.onContentsChanged(slot)
            world!!.notifyBlockUpdate(pos, blockState, blockState, Constants.BlockFlags.BLOCK_UPDATE)
        }

        override fun getSlotLimit(slot: Int): Int = 1
    }

    val inventory = listOf(keeperStackHandler, bookshelfHandler, enchantable)
    var selected: Enchantment? = null
    var selectedLevel: Int = 1
        set(value) {
            this.selected?.let {
                field = MathHelper.clamp(value, it.minLevel, it.maxLevel)
            }
        }
    var info = EnchInfo.NOT_STACK
        private set

    fun setSelected(registryName: String, level: Int = (selected?.minLevel ?: 1)) {
        this.selected = ForgeRegistries.ENCHANTMENTS
            .getValue(ResourceLocation(registryName))
        this.selectedLevel = level
        world!!.notifyBlockUpdate(pos, blockState, blockState, Constants.BlockFlags.BLOCK_UPDATE)
    }

    private fun setInfo(info: EnchInfo) {
        this.info = info
    }

    fun getCost(): Int = if (info.isEnchantable) {
        getEnchantable().repairCost +
                selectedLevel * maxOf(
            1,
            ceil(EnchantmentUtils.getMultiplier(selected!!) * ENoteCommonConfig.ENCHANT_MULTIPLIER.get()).toInt()
        )
    } else {
        -1
    }

    fun checkEnchantable() {
        if (!blockState[EnchantScannerBlock.HAS_TABLE_CLOTH]) {
            setInfo(EnchInfo.NO_TABLE_CLOTH)
            return
        }
        val stack = getEnchantable()
        if (ENoteCommonConfig.ENCHANTED_DISABLED.get() && stack.isEnchanted) {
            setInfo(EnchInfo.ENCHANTED_DISABLED)
        } else if (!stack.isEmpty && selected != null) {
            EnchantmentHelper.getEnchantments(stack).forEach { (ecm, level) ->
                if (ecm === selected) {
                    selectedLevel = level
                    if (level < ecm.maxLevel) {
                        setInfo(EnchInfo.LEVEL_UP)
                    } else {
                        setInfo(EnchInfo.MAX_LEVEL)
                    }
                    return
                } else if (!selected!!.isCompatibleWith(ecm)) {
                    setInfo(EnchInfo.INCOMPATIBLE)
                    return
                }
            }
            if (when (stack.item) {
                    Items.BOOK, Items.ENCHANTED_BOOK -> selected!!.isAllowedOnBooks
                    else -> selected!!.canApply(stack)
                }
            ) {
                setInfo(EnchInfo.OK)
            } else {
                setInfo(EnchInfo.INAPPLICABLE)
            }
        } else {
            info = EnchInfo.NOT_STACK
        }
    }

    override fun getUpdatePacket(): SUpdateTileEntityPacket {
        return SUpdateTileEntityPacket(pos, 1, updateTag)
    }

    override fun onDataPacket(net: NetworkManager?, pkt: SUpdateTileEntityPacket?) {
        super.onDataPacket(net, pkt)
        handleUpdateTag(world!!.getBlockState(pkt!!.pos), pkt.nbtCompound)
    }

    override fun getUpdateTag(): CompoundNBT {
        checkEnchantable()
        val nbt = super.getUpdateTag()
        nbt.put("enchantable", enchantable.getStackInSlot(0).write(CompoundNBT()))
        nbt.putString("selected", selected?.registryName.toString())
        nbt.putInt("level", selectedLevel)
        nbt.putInt("info", info.ordinal)
        return nbt
    }

    override fun handleUpdateTag(state: BlockState?, tag: CompoundNBT?) {
        super.handleUpdateTag(state, tag)
        enchantable.setStackInSlot(0, ItemStack.read(tag!!.getCompound("enchantable")))
        setSelected(tag.getString("selected"), tag.getInt("level"))
        this.info = EnchInfo.values()[tag.getInt("info")]
    }

    fun getKeeper(): LazyOptional<IEnchantKeeper> =
        keeperStackHandler.getStackInSlot(0).getCapability(ENCHANT_KEEPER_CAPABILITY)

    fun getEnchantable(): ItemStack = enchantable.getStackInSlot(0)

    fun checkHasKeeper() {
        val newState = blockState.with(
            EnchantScannerBlock.HAS_KEEPER,
            getKeeper().isPresent
        )
        world!!.setBlockState(pos, newState)
    }

    fun checkBookshelfInv() {
        val newState = blockState.with(
            EnchantScannerBlock.BOOKSHELF_INV,
            minOf(bookshelfHandler.getStackInSlot(1).stack.count / 10, 6)
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
        enchantable.deserializeNBT(nbt.getCompound("enchantable"))
    }

    override fun write(compound: CompoundNBT): CompoundNBT {
        compound.put("keeper", keeperStackHandler.serializeNBT())
        compound.put("books", bookshelfHandler.serializeNBT())
        compound.put("enchantable", enchantable.serializeNBT())
        return super.write(compound)
    }

    fun openNotebookScreen(player: PlayerEntity) {
        if (player is ServerPlayerEntity) {
            getKeeper().ifPresent {
                ENoteNetwork.openKeeper(it, player, pos)
            }
        }
    }

    fun enchant(player: PlayerEntity): Boolean {
        return enchantRes().let {
            if (!it.isEmpty) {
                val cost = getCost()
                if (!player.isCreative) {
                    if (EnchantmentUtils.isCostLimit(cost))
                        return false
                    if (player.experienceLevel < cost)
                        return false
                    player.addExperienceLevel(-cost)
                }
                val raw = getEnchantable()
                if (ENoteCommonConfig.PRIOR_WORK_PENALTY.get() &&
                    EnchantmentHelper.getEnchantments(raw).size >= ENoteCommonConfig.PENALTY_START_NUM.get()
                ) {
                    it.repairCost = RepairContainer.getNewRepairCost(raw.repairCost)
                }
                enchantable.setStackInSlot(0, it)
            }
            !it.isEmpty
        }

    }

    fun enchantRes(): ItemStack {
        var stack = ItemStack.EMPTY
        if (info.isEnchantable && selected != null) {
            val ench = selected!!
            getKeeper().ifPresent {
                val request = 1u shl (selectedLevel - 1)
                val levelI = it[ench]
                if (request > levelI) {
                    return@ifPresent
                }
                it[ench] = levelI - request
                stack = if (getEnchantable().item === Items.BOOK) {
                    ItemStack(Items.ENCHANTED_BOOK, 1)
                } else {
                    getEnchantable().copy()
                }
                val map = EnchantmentHelper.getEnchantments(stack)
                map[ench] = if (map[ench] == selectedLevel) selectedLevel + 1 else selectedLevel
                EnchantmentHelper.setEnchantments(map, stack)
            }
        }
        return stack
    }

    enum class EnchInfo(id: String, val isEnchantable: Boolean = false) {
        OK("ok", true),
        LEVEL_UP("level_up", true),
        MAX_LEVEL("max_level"),
        INCOMPATIBLE("incompatible"),
        NOT_STACK("not_stack"),
        INAPPLICABLE("inapplicable"),

        ENCHANTED_DISABLED("enchanted_disabled"),

        NO_TABLE_CLOTH("no_table_cloth"),
        ;

        val i18nRL = "info.enchantnote.scanner.$id"
    }
}