package be.hize.nes.features.misc

import at.hannibal2.skyhanni.events.LorenzTickEvent
import be.hize.nes.NES
import be.hize.nes.events.GuiRenderEvent
import be.hize.nes.utils.RenderUtils.renderStrings
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class ShowFPS {

    private val config get() = NES.feature.misc.fps
    private var display = emptyList<String>()

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GameOverlayRenderEvent) {
        if (!isEnabled() && Minecraft.getMinecraft().currentScreen == null) return
        config.position.renderStrings(display, posLabel = "FPS")
    }

    private fun update() {
        display = updateDisplay()
    }

    private fun updateDisplay() = buildList {
        add(config.format.replace("%fps%", "${Minecraft.getDebugFPS()}").replace("&", "§"))
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        update()
    }

    private fun isEnabled() = config.enabled
}