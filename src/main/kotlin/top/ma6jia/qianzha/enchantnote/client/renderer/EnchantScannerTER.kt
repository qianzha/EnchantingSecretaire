package top.ma6jia.qianzha.enchantnote.client.renderer

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.block.LecternBlock
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.model.BookModel
import net.minecraft.client.renderer.model.ItemCameraTransforms
import net.minecraft.client.renderer.tileentity.EnchantmentTableTileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.inventory.container.PlayerContainer
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.vector.Quaternion
import net.minecraft.util.math.vector.Vector3f
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import top.ma6jia.qianzha.enchantnote.block.EnchantScannerBlock
import top.ma6jia.qianzha.enchantnote.tileentity.EnchantScannerTE

@OnlyIn(Dist.CLIENT)
class EnchantScannerTER(rendererDispatcherIn: TileEntityRendererDispatcher) :
    TileEntityRenderer<EnchantScannerTE>(rendererDispatcherIn) {

    private val bookModel = BookModel()

    override fun render(
        tileEntityIn: EnchantScannerTE,
        partialTicks: Float,
        matrixStackIn: MatrixStack,
        bufferIn: IRenderTypeBuffer,
        combinedLightIn: Int,
        combinedOverlayIn: Int
    ) {
        val blockState = tileEntityIn.blockState
        val facing = blockState.get(LecternBlock.FACING)
        matrixStackIn.push()
        matrixStackIn.translate(0.5, 1.0625, 0.5)
        val f = facing.rotateY().horizontalAngle
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-f))
        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(67.5f))
        matrixStackIn.translate(0.0, -0.125, 0.0)
        // Render Keeper
        if (blockState.get(EnchantScannerBlock.HAS_KEEPER)) {
            bookModel.setBookState(0.0f, 0.1f, 0.9f, 1.2f)
            val iverTexBuilder =
                EnchantmentTableTileEntityRenderer.TEXTURE_BOOK.getBuffer(bufferIn, RenderType::getEntitySolid)
            bookModel.renderAll(
                matrixStackIn,
                iverTexBuilder,
                combinedLightIn,
                combinedOverlayIn,
                1.0f,
                1.0f,
                1.0f,
                1.0f
            )
        }
        // Render Enchantable Item
        val enchantable = tileEntityIn.getEnchantable()
        if (!enchantable.isEmpty) {
            matrixStackIn.translate(0.1875, 0.0, 0.0)
            matrixStackIn.scale(0.5f, 0.5f, 0.5f)
            matrixStackIn.rotate(Quaternion(0f, 270f, 0f, true))
            val itemRender = Minecraft.getInstance().itemRenderer
            val itemModel = itemRender.getItemModelWithOverrides(enchantable, tileEntityIn.world, null)
            itemRender.renderItem(
                enchantable,
                ItemCameraTransforms.TransformType.FIXED,
                true,
                matrixStackIn,
                bufferIn,
                combinedLightIn,
                combinedOverlayIn,
                itemModel
            )
        }
        matrixStackIn.pop()
        // Render Bookshelf
        renderBookshelfInv(
            matrixStackIn, bufferIn, facing,
            blockState.get(EnchantScannerBlock.BOOKSHELF_INV)
        )
    }

    private fun renderBookshelfInv(
        matrixStackIn: MatrixStack,
        bufferIn: IRenderTypeBuffer,
        facing: Direction,
        num: Int
    ) {
        if (num <= 0) return

        val sprite = Minecraft.getInstance().getAtlasSpriteGetter(
            PlayerContainer.LOCATION_BLOCKS_TEXTURE
        ).apply(
            ResourceLocation("block/lectern_front")
        )
        val builder = bufferIn.getBuffer(RenderType.getSolid())

        val backMinU = sprite.getInterpolatedU(1.0)
        val backMaxU = sprite.getInterpolatedU(1.0 + num)
        val backMinV = sprite.getInterpolatedV(8.0)
        val backMaxV = sprite.getInterpolatedV(12.0)

        val topMinU = sprite.getInterpolatedU(7.0)
        val topMaxU = sprite.getInterpolatedU(7.0 + num)
        val topMinV = sprite.getInterpolatedV(12.0)
        val topMaxV = sprite.getInterpolatedV(13.0)

        val uPix = backMinU - sprite.minU

        val d = 0.0625f
        val h = 0.25f
        val w = 0.0625f * num


        val yDegree = 90 + facing.rotateY().horizontalAngle

        matrixStackIn.push()

        // 11, 5, 4
        when (facing) {
            Direction.NORTH ->
                matrixStackIn.translate(0.6875, 0.5625, 0.25)
            Direction.EAST ->
                matrixStackIn.translate(0.75, 0.5625, 0.6875)
            Direction.SOUTH ->
                matrixStackIn.translate(0.3125, 0.5625, 0.75)
            Direction.WEST ->
                matrixStackIn.translate(0.25, 0.5625, 0.3125)
        }

        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-yDegree))
        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180f))

        add(builder, matrixStackIn, 0f, h, 0f, backMinU, backMaxV)
        add(builder, matrixStackIn, w, h, 0f, backMaxU, backMaxV)
        add(builder, matrixStackIn, w, 0f, 0f, backMaxU, backMinV)
        add(builder, matrixStackIn, 0f, 0f, 0f, backMinU, backMinV)

        add(builder, matrixStackIn, 0f, 0f, d, topMinU, topMinV)
        add(builder, matrixStackIn, 0f, 0f, 0f, topMinU, topMaxV)
        add(builder, matrixStackIn, w, 0f, 0f, topMaxU, topMaxV)
        add(builder, matrixStackIn, w, 0f, d, topMaxU, topMinV)

        add(builder, matrixStackIn, 0f, 0f, d, backMinU, backMinV)
        add(builder, matrixStackIn, 0f, h, d, backMinU, backMaxV)
        add(builder, matrixStackIn, 0f, h, 0f, backMinU + uPix, backMaxV)
        add(builder, matrixStackIn, 0f, 0f, 0f, backMinU + uPix, backMinV)

        matrixStackIn.translate(w.toDouble(), 0.0, 0.0)
        add(builder, matrixStackIn, 0f, 0f, 0.1875f, backMaxU, backMinV)
        add(builder, matrixStackIn, 0f, 0f, 0f, backMaxU - uPix, backMinV)
        add(builder, matrixStackIn, 0f, h, 0f, backMaxU - uPix, backMaxV)
        add(builder, matrixStackIn, 0f, h, 0.1875f, backMaxU, backMaxV)

        matrixStackIn.pop()
    }

    private fun add(
        renderer: IVertexBuilder,
        matrixStackIn: MatrixStack,
        x: Float, y: Float, z: Float,
        u: Float, v: Float
    ) {
        renderer.pos(matrixStackIn.last.matrix, x, y, z)
            .color(1f, 1f, 1f, 1f)
            .tex(u, v)
            .lightmap(0, 240)
            .normal(1f, 0f, 0f)
            .endVertex()
    }

}