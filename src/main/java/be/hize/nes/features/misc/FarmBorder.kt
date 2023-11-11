package be.hize.nes.features.misc

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import be.hize.nes.NES
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

class FarmBorder {

    /*
    This is intended to work for the farm i personally use, change if you want
     */
    private val config get() = NES.feature.misc.farmBorderConfig

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!config.enabled) return
        if (!event.isMod(5)) return
        if (!LorenzUtils.inIsland(IslandType.GARDEN)) return
        if (GardenAPI.toolInHand == null) return
        for (i in 0..config.checkRange) {
            val blockPos1 = LocationUtils.playerLocation().add(LorenzVec(i, 0, 0)).toBlocPos()
            val blockPos2 = LocationUtils.playerLocation().add(LorenzVec(-i, 0, 0)).toBlocPos()
            val block1 = Minecraft.getMinecraft().theWorld.getBlockState(blockPos1).block
            val block2 = Minecraft.getMinecraft().theWorld.getBlockState(blockPos2).block
            if (block1 == Blocks.quartz_block || block2 == Blocks.quartz_block) {
                LorenzUtils.sendTitle("§c§lTURN", 1.seconds)
                SoundUtils.createSound(config.soundName, config.soundPitch, config.soundVolume).playSound()
            } else if (block1 == Blocks.standing_sign || block2 == Blocks.standing_sign) {
                LorenzUtils.sendTitle("§c§lWARP", 1.seconds)
                SoundUtils.createSound(config.soundNameWarp, config.soundPitch, config.soundVolume).playSound()
            }
        }
    }
}