package be.hize.nes.features.misc

import at.hannibal2.skyhanni.data.ClickType
import at.hannibal2.skyhanni.events.BlockClickEvent
import at.hannibal2.skyhanni.utils.LorenzUtils.chat
import be.hize.nes.NES
import be.hize.nes.events.ConfigLoadEvent
import be.hize.nes.mixin.hooks.colorList
import be.hize.nes.mixin.hooks.updated
import be.hize.nes.utils.NESUtils.onToggle
import net.minecraft.client.Minecraft
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object Ghost {

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