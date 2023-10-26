package be.hize.nes.features.misc

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.toLorenzVec
import be.hize.nes.NES
import net.minecraft.client.Minecraft
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityChest
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

class PowderChest {

    private val enabled get() = NES.feature.misc.highlightPowderChest

    @SubscribeEvent
    fun onRender(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        getTilesNearby<TileEntityChest>(LocationUtils.playerLocation(), 15.0).forEach {
            event.drawWaypointFilled(it.pos.toLorenzVec(), Color.GREEN, seeThroughBlocks = true)
        }
    }

    private inline fun <reified T : TileEntity> getTilesNearby(location: LorenzVec, radius: Double): Sequence<T> =
        getTiles<T>().filter { it.pos.toLorenzVec().distanceToPlayer() <= radius }

    private inline fun <reified R : TileEntity> getTiles(): Sequence<R> = getAllTiles().filterIsInstance<R>()

    private fun getAllTiles(): Sequence<TileEntity> = Minecraft.getMinecraft()?.theWorld?.loadedTileEntityList?.let {
        if (Minecraft.getMinecraft().isCallingFromMinecraftThread) it else it.toMutableList()
    }?.asSequence() ?: emptySequence()

    private fun isEnabled() = LorenzUtils.inSkyBlock && LorenzUtils.skyBlockIsland == IslandType.CRYSTAL_HOLLOWS && enabled

}