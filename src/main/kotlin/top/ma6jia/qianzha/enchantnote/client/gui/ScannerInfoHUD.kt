package top.ma6jia.qianzha.enchantnote.client.gui

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.AbstractGui
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import top.ma6jia.qianzha.enchantnote.block.EnchantScannerBlock
import top.ma6jia.qianzha.enchantnote.tileentity.EnchantScannerTE
import top.ma6jia.qianzha.enchantnote.utils.EnchantmentUtils

@OnlyIn(Dist.CLIENT)
object ScannerInfoHUD : AbstractGui() {
    private val TEXT_EXPENSIVE = TranslationTextComponent("container.repair.expensive")
    private const val COLOR_INFO = 0x00_FF_00
    private const val COLOR_WARN = 0xEC_1C_24

    private val mc: Minecraft
        get() = Minecraft.getInstance()
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
                    tile.blockState[EnchantScannerBlock.HAS_KEEPER]
                ) {
                    tile.selected?.let { ecm ->
                        val xC = mc.mainWindow.scaledWidth.toFloat() / 2f
                        val yC = mc.mainWindow.scaledHeight.toFloat() / 2f
                        val fontRenderer = mc.fontRenderer!!
                        val text = ecm.getDisplayName(tile.selectedLevel) as TextComponent
                        val ecmWidth = fontRenderer.getStringPropertyWidth(text)
                        fontRenderer.drawTextWithShadow(matrix, text, xC - ecmWidth, yC, 0)
                        val info = TranslationTextComponent(tile.info.i18nRL)

                        fontRenderer.drawTextWithShadow(
                            matrix, info, xC, yC,
                            if (tile.info.isEnchantable) COLOR_INFO else COLOR_WARN
                        )

                        val cost = tile.getCost()
                        if (!player.isCreative && cost > 0) {
                            val limited = EnchantmentUtils.isCostLimit(cost)
                            fontRenderer.drawTextWithShadow(
                                matrix,
                                if (limited) {
                                    TEXT_EXPENSIVE
                                } else {
                                    StringTextComponent("附魔花费：$cost")
                                },
                                xC, yC + fontRenderer.FONT_HEIGHT,
                                if (!limited && player.experienceLevel >= cost) {
                                    COLOR_INFO
                                } else {
                                    COLOR_WARN
                                }
                            )
                        }

                    }
                }
            }
        }
    }

}