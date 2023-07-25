package be.hize.nes.config

import be.hize.nes.NES
import io.github.moulberry.moulconfig.gui.GuiScreenElementWrapper
import io.github.moulberry.moulconfig.gui.MoulConfigEditor

object ConfigGuiManager {
    val configEditor by lazy { MoulConfigEditor(NES.configManager.processor) }

    fun openConfigGui(search: String? = null) {
        if (search != null) {
            configEditor.search(search)
        }
        NES.screenToOpen = GuiScreenElementWrapper(configEditor)
    }


}