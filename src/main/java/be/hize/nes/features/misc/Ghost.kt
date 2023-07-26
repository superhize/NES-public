package be.hize.nes.features.misc

import be.hize.nes.NES
import be.hize.nes.events.ConfigLoadEvent
import be.hize.nes.mixin.hooks.updated
import be.hize.nes.utils.NESUtils.onToggle
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class Ghost {

    private val config get() = NES.feature.misc.ghost

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        onToggle(config.color,
            config.recolorMist) {
            Minecraft.getMinecraft().renderGlobal.loadRenderers()
        }

        onToggle(config.recolorCreeper,
            config.creeperColor) {
            updated = false
            Minecraft.getMinecraft().renderGlobal.loadRenderers()
        }

    }
}