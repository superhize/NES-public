package be.hize.nes.features.misc

import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.addAsSingletonList
import be.hize.nes.NES
import be.hize.nes.events.GuiRenderEvent
import be.hize.nes.utils.RenderUtils.renderStrings
import be.hize.nes.utils.RenderUtils.renderStringsAndItems
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class ShowFPS {

    private val config get() = NES.feature.misc.fps
    private var display = emptyList<List<Any>>()

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GameOverlayRenderEvent) {
        if (!isEnabled()) return
        if (display.isNotEmpty()){
            config.position.renderStringsAndItems(
                display,
                posLabel = "FPS")
        }
    }

    private fun update() {
        display = updateDisplay()
    }

    private fun updateDisplay() = buildList {
        val fps = Minecraft.getDebugFPS().toString()
        addAsSingletonList(config.format.replace("%fps%", fps).replace("&", "§"))
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        update()
    }

    private fun isEnabled() = config.enabled && LorenzUtils.inSkyBlock
}