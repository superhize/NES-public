package be.hize.nes.features.misc.coordinate

import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.utils.LorenzUtils.addAsSingletonList
import be.hize.nes.NES
import be.hize.nes.events.GuiRenderEvent
import be.hize.nes.utils.RenderUtils.renderStringsAndItems
import be.hize.nes.utils.decimalFormat
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.text.DecimalFormat

class ShowCoordinate {
    private val config get() = NES.feature.misc.coordinate
    private var display = emptyList<List<Any>>()

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GameOverlayRenderEvent) {
        if (!isEnabled()) return
        config.position.renderStringsAndItems(display, posLabel = "Coordinate")
    }

    private fun update() {
        display = updateDisplay()
    }

    private fun updateDisplay() = buildList {
        val p = Minecraft.getMinecraft().thePlayer
        if (p == null) emptyList<List<Any>>()
        val df: DecimalFormat = decimalFormat(0, false, false)

        addAsSingletonList("§6X: §b${df.format(Minecraft.getMinecraft().thePlayer.posX)}")
        addAsSingletonList("§6Y: §b${df.format(Minecraft.getMinecraft().thePlayer.posY)}")
        addAsSingletonList("§6Z: §b${df.format(Minecraft.getMinecraft().thePlayer.posZ)}")
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        update()
    }

    private fun isEnabled() = config.enabled
}