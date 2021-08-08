package top.ma6jia.qianzha.enchantnote.block

import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.block.material.MaterialColor
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItemUseContext
import net.minecraft.state.BooleanProperty
import net.minecraft.state.DirectionProperty
import net.minecraft.state.IntegerProperty
import net.minecraft.state.StateContainer
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.Direction
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.shapes.IBooleanFunction
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import top.ma6jia.qianzha.enchantnote.EnchantNote
import top.ma6jia.qianzha.enchantnote.capability.ENoteCapability
import top.ma6jia.qianzha.enchantnote.tileentity.EnchantScannerTE
import java.awt.print.Book


class EnchantScannerBlock() : Block(
    (
            Properties.create(Material.ANVIL)
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
        return when(state.get(FACING)) {
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
        val tileEntity = worldIn.getTileEntity(pos)
        if (tileEntity is EnchantScannerTE) {
            tileEntity.inventory.forEach {
                for (i in 0 until it.slots) {
                    spawnAsEntity(worldIn, pos, it.getStackInSlot(i))
                }
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player)
    }

    override fun onBlockClicked(state: BlockState, worldIn: World, pos: BlockPos, player: PlayerEntity) {
        val tileEntity = worldIn.getTileEntity(pos)
        if (tileEntity is EnchantScannerTE) {
            val inv = tileEntity.inventory
            for (i in (inv.size - 1) downTo 0) {
                val stack = inv[i].extractItem(0, 64, false)
                if (!stack.isEmpty) {
                    spawnAsEntity(worldIn, pos.up(), stack)
                    return
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
        if (!held.isEmpty && tileEntity is EnchantScannerTE) {
            tileEntity.inventory.forEach {
                val rest = it.insertItem(0, held, false)
                if (rest.count !== held.count) {
                    player.setHeldItem(handIn, rest)
                    return ActionResultType.SUCCESS
                }
            }
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit)
    }

}