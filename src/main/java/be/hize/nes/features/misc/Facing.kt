package be.hize.nes.features.misc

import at.hannibal2.skyhanni.events.LorenzTickEvent
import be.hize.nes.NES
import be.hize.nes.events.GuiRenderEvent
import be.hize.nes.features.misc.coordinate.Facing
import be.hize.nes.utils.RenderUtils.renderStrings
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class Facing {

    private val config get() = NES.feature.misc.facing
    private var display = emptyList<String>()

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GameOverlayRenderEvent) {
        if (!isEnabled()) return
        config.position.renderStrings(display, posLabel = "Facing")
    }

    private fun update() {
        display = updateDisplay()
    }

    private fun updateDisplay() = buildList {

        if (Minecraft.getMinecraft().thePlayer != null) {
            add("§6Facing: §b${Facing.parse(Minecraft.getMinecraft().thePlayer.rotationYaw).abbreviated}")
        }else{
            return emptyList<String>()
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        update()
    }

    private fun isEnabled() = config.enabled

}