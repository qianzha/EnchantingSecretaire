package top.ma6jia.qianzha.enchantnote.block

import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.block.material.MaterialColor
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.particles.ParticleTypes
import net.minecraft.state.BooleanProperty
import net.minecraft.state.DirectionProperty
import net.minecraft.state.IntegerProperty
import net.minecraft.state.StateContainer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.shapes.IBooleanFunction
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.util.Constants
import top.ma6jia.qianzha.enchantnote.item.ENoteItems
import top.ma6jia.qianzha.enchantnote.tileentity.EnchantScannerTE


class EnchantScannerBlock : Block(
    (
            Properties.create(Material.ANVIL, MaterialColor.IRON)
                .hardnessAndResistance(5.0F, 1200.0F)
                .sound(SoundType.ANVIL)
                .notSolid()
            )
) {
    companion object {
        val FACING: DirectionProperty = HorizontalBlock.HORIZONTAL_FACING
        val HAS_TABLE_CLOTH: BooleanProperty = BooleanProperty.create("has_table_cloth")
        val HAS_KEEPER: BooleanProperty = BooleanProperty.create("has_keeper")
        val BOOKSHELF_INV: IntegerProperty = IntegerProperty.create("bookshelf_inv", 0, 6)

        private val PART_RM = makeCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0)
        private val PART_BASE = makeCuboidShape(2.0, 0.0, 2.0, 14.0, 4.0, 14.0)
        val NORTH_SHAPE: VoxelShape = VoxelShapes.or(
            VoxelShapes.combineAndSimplify(LecternBlock.NORTH_SHAPE, PART_RM, IBooleanFunction.ONLY_FIRST),
            PART_BASE
        )
        val EAST_SHAPE: VoxelShape = VoxelShapes.or(
            VoxelShapes.combineAndSimplify(LecternBlock.EAST_SHAPE, PART_RM, IBooleanFunction.ONLY_FIRST),
            PART_BASE
        )
        val SOUTH_SHAPE: VoxelShape = VoxelShapes.or(
            VoxelShapes.combineAndSimplify(LecternBlock.SOUTH_SHAPE, PART_RM, IBooleanFunction.ONLY_FIRST),
            PART_BASE
        )
        val WEST_SHAPE: VoxelShape = VoxelShapes.or(
            VoxelShapes.combineAndSimplify(LecternBlock.WEST_SHAPE, PART_RM, IBooleanFunction.ONLY_FIRST),
            PART_BASE
        )
    }

    init {
        defaultState = this.stateContainer.baseState
            .with(FACING, Direction.NORTH)
            .with(HAS_TABLE_CLOTH, false)
            .with(HAS_KEEPER, false)
            .with(BOOKSHELF_INV, 0)
    }

    override fun fillStateContainer(builder: StateContainer.Builder<Block, BlockState>) {
        builder.add(FACING, HAS_TABLE_CLOTH, HAS_KEEPER, BOOKSHELF_INV)
        super.fillStateContainer(builder)
    }

    override fun getStateForPlacement(context: BlockItemUseContext?): BlockState? {
        return this.defaultState
            .with(FACING, context!!.placementHorizontalFacing.opposite)
    }

    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    override fun getShape(
        state: BlockState,
        worldIn: IBlockReader,
        pos: BlockPos,
        context: ISelectionContext
    ): VoxelShape {
        return when (state.get(FACING)) {
            Direction.EAST -> EAST_SHAPE
            Direction.SOUTH -> SOUTH_SHAPE
            Direction.WEST -> WEST_SHAPE
            else -> NORTH_SHAPE
        }
    }


    override fun hasTileEntity(state: BlockState?) = true

    override fun createTileEntity(state: BlockState?, world: IBlockReader?): TileEntity {
        return EnchantScannerTE()
    }

    override fun onBlockHarvested(worldIn: World, pos: BlockPos, state: BlockState, player: PlayerEntity) {
        super.onBlockHarvested(worldIn, pos, state, player)
        val tileEntity = worldIn.getTileEntity(pos)
        if (tileEntity is EnchantScannerTE) {
            tileEntity.inventory.forEach {
                for (i in 0 until it.slots) {
                    spawnAsEntity(worldIn, pos, it.getStackInSlot(i))
                }
            }
        }
    }

    override fun onBlockClicked(state: BlockState, worldIn: World, pos: BlockPos, player: PlayerEntity) {
        @Suppress("DEPRECATION")
        if (worldIn.isRemote)
            super.onBlockClicked(state, worldIn, pos, player)
        val tileEntity = worldIn.getTileEntity(pos)
        if (tileEntity is EnchantScannerTE) {
            if (player.heldItemMainhand.item == Items.STICK) {
                val maxLevel = tileEntity.selected?.maxLevel ?: 1
                tileEntity.selectedLevel = (tileEntity.selectedLevel % maxLevel) + 1
                worldIn.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE)
            } else {
                val inv = tileEntity.inventory
                for (i in (inv.size - 1) downTo 0) {
                    val stack = inv[i].extractItem(inv[i].slots - 1, 64, false)
                    if (!stack.isEmpty) {
                        spawnAsEntity(worldIn, pos.up(), stack)
                        return
                    }
                }
                if (state[HAS_TABLE_CLOTH]) {
                    spawnAsEntity(worldIn, pos.up(), ItemStack(ENoteItems.ENCHANT_TABLE_CLOTH, 1))
                    worldIn.setBlockState(pos, state.with(HAS_TABLE_CLOTH, false))
                }
            }
        }
    }

    override fun onBlockActivated(
        state: BlockState,
        worldIn: World,
        pos: BlockPos,
        player: PlayerEntity,
        handIn: Hand,
        hit: BlockRayTraceResult
    ): ActionResultType {
        val tileEntity = worldIn.getTileEntity(pos)
        val held = player.getHeldItem(handIn)
        if (worldIn.isRemote && tileEntity is EnchantScannerTE) {
            if (tileEntity.inventory[2].insertItem(0, held, true).isEmpty)
                return ActionResultType.SUCCESS
        }
        @Suppress("DEPRECATION")
        if (worldIn.isRemote || handIn == Hand.OFF_HAND) {
            return super.onBlockActivated(state, worldIn, pos, player, handIn, hit)
        }
        if (tileEntity is EnchantScannerTE) {
            if (!held.isEmpty) {
                if (held.item == Items.STICK) {
                    if (state.get(HAS_TABLE_CLOTH) && tileEntity.enchant(player)) {
                        worldIn.playSound(
                            null,
                            pos,
                            SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                            SoundCategory.BLOCKS,
                            1.0F,
                            worldIn.rand.nextFloat() * 0.1F + 0.9F
                        )
                    } else {
                        (worldIn as ServerWorld).spawnParticle(
                            ParticleTypes.LARGE_SMOKE,
                            pos.x + 0.5,
                            pos.y + 0.25,
                            pos.z + 0.5,
                            8,
                            0.5,
                            0.25,
                            0.5,
                            0.0
                        )

                    }
                } else if (held.item == ENoteItems.ENCHANT_TABLE_CLOTH) {
                    worldIn.setBlockState(pos, state.with(HAS_TABLE_CLOTH, true))
                    if (!player.isCreative) held.shrink(1)
                } else {
                    val stack = held.copy()
                    stack.count = 1
                    tileEntity.inventory.forEach {
                        if (it.insertItem(0, stack, false).isEmpty) {
                            if (!player.isCreative) held.shrink(1)
                            player.setHeldItem(handIn, held)
                            return ActionResultType.SUCCESS
                        }
                    }
                }
            } else {
                tileEntity.openNotebookScreen(player)
            }
        }
        @Suppress("DEPRECATION")
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit)
    }

}