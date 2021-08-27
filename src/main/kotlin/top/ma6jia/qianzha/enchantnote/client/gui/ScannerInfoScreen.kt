package top.ma6jia.qianzha.enchantnote.client.gui

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.AbstractGui
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import top.ma6jia.qianzha.enchantnote.block.EnchantScannerBlock
import top.ma6jia.qianzha.enchantnote.tileentity.EnchantScannerTE

@OnlyIn(Dist.CLIENT)
class ScannerInfoScreen : AbstractGui() {
    companion object {
        val TEXT_EXPENSIVE = TranslationTextComponent("container.repair.expensive")
    }

    private val mc: Minecraft = Minecraft.getInstance()
    var pos: BlockPos? = null

    fun render(matrix: MatrixStack) {
        val world = mc.world
        val player = mc.player
        if (
            world != null &&
            player != null &&
            player.getHeldItem(Hand.MAIN_HAND).item == Items.STICK
        ) {
            pos?.let { pos ->
                val tile = world.getTileEntity(pos)
                if (
                    tile is EnchantScannerTE &&
                    tile.blockState[EnchantScannerBlock.HAS_KEEPER] &&
                    tile.blockState[EnchantScannerBlock.HAS_TABLE_CLOTH]
                ) {
                    tile.selected?.let { ecm ->
                        val width = mc.mainWindow.scaledWidth.toFloat()
                        val height = mc.mainWindow.scaledHeight.toFloat()
                        val fontRenderer = mc.fontRenderer!!
                        val text = ecm.getDisplayName(tile.selectedLevel) as TextComponent
                        val ecmWidth = fontRenderer.getStringPropertyWidth(text)
                        fontRenderer.drawTextWithShadow(matrix, text, width / 2f - ecmWidth, height / 2f, 0)
                        val info = TranslationTextComponent(tile.info.i18nRL)
                        val cost = tile.getCost()
                        if (cost > 0) {
                            if (tile.info != EnchantScannerTE.EnchInfo.COST_LIMIT || player.isCreative) {
                                info.appendString(" \n附魔花费：$cost")
                            } else {
                                info.appendSibling(TEXT_EXPENSIVE)
                            }
                        }
                        fontRenderer.drawTextWithShadow(matrix, info, width / 2f, height / 2f, 0xEC_1C_24)
                    }
                }
            }
        }
    }
}