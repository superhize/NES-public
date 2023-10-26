package be.hize.nes.features.misc

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.RenderEntityOutlineEvent
import at.hannibal2.skyhanni.events.withAlpha
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.*
import at.hannibal2.skyhanni.utils.EntityUtils.hasSkullTexture
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine
import at.hannibal2.skyhanni.utils.RenderUtils.draw3DLine_nea
import at.hannibal2.skyhanni.utils.RenderUtils.drawFilledBoundingBox_nea
import at.hannibal2.skyhanni.utils.RenderUtils.exactLocation
import at.hannibal2.skyhanni.utils.RenderUtils.exactPlayerEyeLocation
import at.hannibal2.skyhanni.utils.SoundUtils.playSound
import be.hize.nes.NES
import be.hize.nes.events.ConfigLoadEvent
import be.hize.nes.utils.NESUtils.afterChange
import be.hize.nes.utils.RenderUtils.exactLocation
import be.hize.nes.utils.RenderUtils.exactPlayerEyeLocation
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraft.entity.passive.EntityRabbit
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

class MobHighlighter {


    private val config get() = NES.feature.misc.mobHighlighterConfig
    private var entityList = mutableMapOf<String, Int>()

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        config.mobToHighlight.afterChange {
            update()
        }
        update()
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        if (!event.isMod(10)) return
        if (config.renderType == 0 || config.renderType == 2) {
            EntityUtils.getAllEntities().filterIsInstance<EntityCreature>().forEach {
                val name = it::class.java.simpleName
                if (entityList.contains(name)) {
                    RenderLivingEntityHelper.setEntityColor(
                        it,
                        Color.GREEN.withAlpha(80)
                    ) { isEnabled() }
                    RenderLivingEntityHelper.setNoHurtTime(it) { isEnabled() }
                }
            }

            if (LorenzUtils.skyBlockIsland == IslandType.CRYSTAL_HOLLOWS && config.butterfly){
                EntityUtils.getAllEntities().filterIsInstance<EntityArmorStand>().forEach {
                    if (it.getAllNameTagsInRadiusWith("Butterfly").isNotEmpty()) {
                        RenderLivingEntityHelper.setEntityColor(
                            it,
                            Color.GREEN.withAlpha(80)
                        ) { isEnabled() && config.butterfly }
                        RenderLivingEntityHelper.setNoHurtTime(it) { isEnabled() }
                    }
                }
            }
        }
    }

    private fun EntityLivingBase.getAllNameTagsInRadiusWith(
        contains: String,
        radius: Double = 3.0,
    ): List<EntityArmorStand> {
        val center = getLorenzVec().add(0, 3, 0)
        val a = center.add(-radius, -radius - 3, -radius).toBlocPos()
        val b = center.add(radius, radius + 3, radius).toBlocPos()
        val alignedBB = AxisAlignedBB(a, b)
        val clazz = EntityArmorStand::class.java
        val found = worldObj.getEntitiesWithinAABB(clazz, alignedBB)
        return found.filter {
            val result = it.name.contains(contains)
            result
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderEntityOutlines(event: RenderEntityOutlineEvent) {
        if (!isEnabled()) return
        if (config.renderType == 1 || config.renderType == 2) {
            event.queueEntitiesToOutline { entity -> parse(entity) }
        }
    }

    private fun parse(entity: Entity): Int? {
        if (entity is EntityArmorStand && entity.getAllNameTagsInRadiusWith("Butterfly").isNotEmpty()) return 0X9900FF
        return entityList[entity::class.java.simpleName]
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onWorldRender(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (LorenzUtils.skyBlockIsland == IslandType.CRYSTAL_HOLLOWS && config.butterfly){
            EntityUtils.getAllEntities().filterIsInstance<EntityArmorStand>().forEach { it ->
                it.getAllNameTagsInRadiusWith("Butterfly").forEach{
                    event.draw3DLine(
                        event.exactPlayerEyeLocation(),
                        event.exactLocation(it).add(0.0, 1.5, 0.0),
                        Color.RED,
                        3,
                        true
                    )
                }
            }
        }
        if (LorenzUtils.skyBlockIsland == IslandType.DEEP_CAVERNS && config.sneakyShit){
            EntityUtils.getAllEntities().filterIsInstance<EntityCreeper>().forEach {
                val me = Minecraft.getMinecraft().thePlayer
                event.draw3DLine_nea(
                    event.exactPlayerEyeLocation(),
                    event.exactLocation(it).add(0.0, 1.54, 0.0),
                    Color.GREEN,
                    2,
                    true
                )
                val axis = it.entityBoundingBox
                event.drawFilledBoundingBox_nea(axis, Color.GREEN, 0.8F, false, false)
            }
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun pre(event: RenderLivingEvent.Pre<*>) {
        if (!isEnabled()) return
        val entity = event.entity
        if (entity is EntityCreature) {
            if (!entityList.contains(event.entity::class.java.simpleName)) return
            GlStateManager.disableDepth()
        } else if (entity is EntityArmorStand) {
            if (!config.butterfly) return
            if (entity.getAllNameTagsInRadiusWith("Butterfly").isNotEmpty()) {
                GlStateManager.disableDepth()
            }

        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun pre(event: RenderLivingEvent.Post<*>) {
        if (!isEnabled()) return
        val entity = event.entity
        if (entity is EntityCreature) {
            if (!entityList.contains(event.entity::class.java.simpleName)) return
            GlStateManager.enableDepth()
        } else if (entity is EntityArmorStand) {
            if (!config.butterfly) return
            if (entity.getAllNameTagsInRadiusWith("Butterfly").isEmpty()) return
            GlStateManager.enableDepth()
        }
    }


    private fun update() {
        entityList.clear()
        for (index in config.mobToHighlight.get()) {
            entityList[Mob.entries[index].clazz] = Mob.entries[index].color
        }
    }

    enum class Mob(val clazz: String, val color: Int) {
        IRON_GOLEM("EntityIronGolem", 0XFF0000),
        ZOMBIE("EntityZombie", 0x00FF00),
        SKELETON("EntitySkeleton", 0xF3F3F3),
        ENDERMAN("EntityEnderman", 0xFF00FF),
        ENDERMITE("EntityEndermite", 0x9900FF),
        COW("EntityCow", 0XA64D79),
        CHICKEN("EntityChicken", 0xF3F3F3),
        HORSE("EntityHorse", 0XF3F3F3),
        MUSHROOM("EntityMooshroom", 0XE06666),
        WOLF("EntityWolf", 0XF3F3F3),
        BLAZE("EntityBlaze", 0XFFD966),
        CAVE_SPIDER("EntityCaveSpider", 0X93C47D),
        CREEPER("EntityCreeper", 0X00FF00),
        GHAST("EntityGhast", 0XFFD966),
        MAGMA_CUBE("EntityMagmaCube", 0XC0C000),
        PIG_ZOMBIE("EntityPigZombie", 0XC0C000),
        SILVERFISH("EntitySilverFish", 0XEEEEEE),
        SLIME("EntitySlime", 0X00FF00),
        SPIDER("EntitySpider", 0X783F04),
        WITHER("EntityWither", 0X9900FF),
        DRAGON("EntityDragon", 0X9900FF),
        RABBIT("EntityRabbit", 0XFFFFFF),
        PIG("EntityPig", 0XFF0000),
        SHEEP("EntitySheep", 0XFFFFFF),
        WITHER_SKELETON("EntityWitherSkeleton", 0XFFFFFF)
    }

    private fun isEnabled() = config.enabled && LorenzUtils.inSkyBlock
}