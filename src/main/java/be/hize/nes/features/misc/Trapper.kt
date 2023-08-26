package be.hize.nes.features.misc

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.utils.LorenzUtils
import be.hize.nes.NES
import be.hize.nes.utils.NESEntityUtils
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.passive.*
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

class Trapper {

    private val config get() = NES.feature.misc.trapper
    private var color: Color? = null
    private val trapperRegex =
        Regex("^§e\\[NPC\\] Trevor§f: §rYou can find your §(?<color>[0-9a-f])§l\\w+ §fanimal near the §(?<locationColor>[0-9a-f])(?<location>[\\w ]+)§f.§r$")
    private val animals = listOf(
        EntityCow::class,
        EntityPig::class,
        EntitySheep::class,
        EntityCow::class,
        EntityChicken::class,
        EntityRabbit::class,
        EntityHorse::class
    )
    private val animalHp: List<Float?> = listOf(100F, 200F, 500F, 1000F, 1024F, 2048F)

    private fun isTrapperAnimal(entity: Entity): Boolean =
        entity.ticksExisted >= 20 && (entity::class in animals) && animalHp.contains((entity as? EntityLiving)?.maxHealth)

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (LorenzUtils.skyBlockIsland != IslandType.THE_FARMING_ISLANDS || event.message == null || event.type == 2.toByte()) return
        val matchResult = event.message.formattedText.let { trapperRegex.find(it) }
        if (matchResult != null) {
            val colorCode = (matchResult.groups as? MatchNamedGroupCollection)?.get("color")?.value ?: return
            color = Color(Minecraft.getMinecraft().fontRendererObj.getColorCode(colorCode.single()))
        } else if (event.message.formattedText.startsWith("§r§aKilling the animal rewarded you ") || event.message.formattedText.startsWith(
                "§r§aYour mob died randomly, you are rewarded "
            )
        ) {
            color = null
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!isEnabled()) return
        if (LorenzUtils.skyBlockIsland != IslandType.THE_FARMING_ISLANDS) return
        Minecraft.getMinecraft().theWorld.loadedEntityList.forEach {
            if (color != null && isTrapperAnimal(it)) {
                val renderManager = Minecraft.getMinecraft().renderManager
                val x = it.lastTickPosX + (it.posX - it.lastTickPosX) * event.partialTicks - renderManager.viewerPosX
                val y = it.lastTickPosY + (it.posY - it.lastTickPosY) * event.partialTicks - renderManager.viewerPosY
                val z = it.lastTickPosZ + (it.posZ - it.lastTickPosZ) * event.partialTicks - renderManager.viewerPosZ
                val entityHeight = it.entityBoundingBox.maxY - it.entityBoundingBox.minY
                NESEntityUtils.drawEntityBox(
                    it, color!!, outline = true, fill = true, partialTicks = event.partialTicks, esp = true
                )
                NESEntityUtils.renderBeaconBeam(x - 0.5, y + entityHeight, z - 0.5, color!!, 1F, event.partialTicks, true)
            }
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled

}