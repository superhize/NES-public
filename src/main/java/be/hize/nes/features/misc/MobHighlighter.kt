package be.hize.nes.features.misc

import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.withAlpha
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import be.hize.nes.NES
import be.hize.nes.events.ConfigLoadEvent
import be.hize.nes.utils.NESUtils.afterChange
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityEndermite
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraft.entity.monster.EntityZombie
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

class MobHighlighter {


    private val config get() = NES.feature.misc.mobHighlighterConfig
    private var entityList = mutableListOf<String>()

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
        if (!event.isMod(5)) return
        EntityUtils.getAllEntities().filterIsInstance<EntityCreature>().forEach {
            val name = it::class.java.simpleName
            if (entityList.contains(name)){
                RenderLivingEntityHelper.setEntityColor(it, Color.GREEN.withAlpha(80)) { isEnabled() && entityList.contains(name) }
                RenderLivingEntityHelper.setNoHurtTime(it) {  isEnabled() && entityList.contains(name) }
            }
        }
    }

    @SubscribeEvent
    fun pre(event: RenderLivingEvent.Pre<EntityCreature>) {
        if (!isEnabled()) return
        if (!entityList.contains(event.entity::class.java.simpleName)) return
        GlStateManager.disableDepth()
    }

    @SubscribeEvent
    fun pre(event: RenderLivingEvent.Post<EntityCreature>) {
        if (!isEnabled()) return
        if (!entityList.contains(event.entity::class.java.simpleName)) return
        GlStateManager.enableDepth()
    }

    private fun update() {
        entityList.clear()
        for (index in config.mobToHighlight.get()) {
            entityList.add(Mob.entries[index].clazz)
        }
    }

    enum class Mob(val clazz: String) {
        IRON_GOLEM("EntityIronGolem"),
        ZOMBIE("EntityZombie"),
        SKELETON("EntitySkeleton"),
        ENDERMAN("EntityEnderman"),
        ENDERMITE("EntityEndermite"),

    }

    private fun isEnabled() = config.enabled && LorenzUtils.inSkyBlock
}