package be.hize.nes.features.misc

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.withAlpha
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.EntityUtils.getAllNameTagsInRadiusWith
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.editCopy
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import be.hize.nes.NES
import net.minecraft.entity.monster.EntityGhast
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

class PowderGhastHighlight {

    private val areaPattern = "§eFind the §r§6Powder Ghast§r§e near the §r§b(?<area>.*)§r§e!".toPattern()
    private val entityList = mutableListOf<EntityGhast>()
    private var area = ""

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        areaPattern.matchMatcher(event.message) {
            area = group("area")
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent){
        if (LorenzUtils.inSkyBlock && NES.feature.misc.highlightPowderGhast && LorenzUtils.skyBlockIsland == IslandType.DWARVEN_MINES) {
            if (event.isMod(20)){
                EntityUtils.getEntities<EntityGhast>().forEach {
                    if (!entityList.contains(it)) entityList.add(it)
                }
            }
            if (event.isMod(10)){
                entityList.editCopy { removeIf { !it.isEntityAlive } }
            }
        }
    }
    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (LorenzUtils.inSkyBlock && NES.feature.misc.highlightPowderGhast && LorenzUtils.skyBlockIsland == IslandType.DWARVEN_MINES) {
            entityList.forEach {
                if (it.isEntityAlive){
                    var text = "§cGhast "
                    it.getAllNameTagsInRadiusWith("❤", 10.0).forEach { stand ->
                        text += "§8\\[§7Lv1§8] §cPowder Ghast§r (?<hp>.*)".toRegex().find(stand.name)?.groupValues?.get(1)
                            ?: ""
                    }
                    event.drawDynamicText(
                        event.exactLocation(it),
                        text,
                        1.5,
                        ignoreBlocks = true
                    )
                    RenderLivingEntityHelper.setEntityColor(it, LorenzColor.GREEN.toColor().withAlpha(80)) { true }
                    RenderLivingEntityHelper.setNoHurtTime(it) { true }
                    for (loc in Location.entries) {
                        if (loc.loc == area) {
                            event.drawWaypointFilled(loc.vec,
                                Color.GREEN,
                                seeThroughBlocks = true,
                                beacon = true)
                        }
                    }
                }
            }

        }
    }
    enum class Location(val loc: String, val vec: LorenzVec) {
        DIVAN_GATEAWAY("Divan's Gateway", LorenzVec(12, 162, 113)),
        RAMPART_QUARRY("Rampart's Quarry", LorenzVec(-99, 176, -16)),
        CLIFFSIDE_VEINES("Cliffside Veins", LorenzVec(34, 13, 30)),
        FORGE_BASSIN("Forge Basin", LorenzVec(9, 189, -6))
    }
}