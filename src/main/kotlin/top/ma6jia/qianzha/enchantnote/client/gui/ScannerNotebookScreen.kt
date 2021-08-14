package top.ma6jia.qianzha.enchantnote.client.gui

import net.minecraft.client.gui.DialogTexts
import net.minecraft.client.gui.screen.ReadBookScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.button.Button
import net.minecraft.util.math.BlockPos
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import top.ma6jia.qianzha.enchantnote.capability.IEnchantKeeper
import top.ma6jia.qianzha.enchantnote.network.ENoteNetwork
import top.ma6jia.qianzha.enchantnote.network.message.ScannerSelectMsg


@OnlyIn(Dist.CLIENT)
class ScannerNotebookScreen(
    private val bookInfoIn: IEnchantKeeper,
    private val pos: BlockPos?
) : ReadBookScreen(bookInfoIn) {
    override fun addDoneButton() {
        addButton(Button(
            width / 2 - 100, 196, 200, 20, DialogTexts.GUI_DONE
        ) {
            pos?.let {
                ENoteNetwork.CHANNEL.sendToServer(ScannerSelectMsg(
                    pos, bookInfoIn.getCurrent()?.registryName?.toString() ?: ""
                ))
            }
            minecraft!!.displayGuiScreen(
                null as Screen?
            )
        })
    }
}