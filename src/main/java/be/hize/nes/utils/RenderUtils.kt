package be.hize.nes.utils


import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils
import be.hize.nes.config.core.config.Position
import be.hize.nes.data.GuiEditManager
import be.hize.nes.data.GuiEditManager.Companion.getAbsX
import be.hize.nes.data.GuiEditManager.Companion.getAbsY
import be.hize.nes.utils.renderables.Renderable
import io.github.moulberry.notenoughupdates.util.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.inventory.Slot
import net.minecraftforge.client.event.RenderWorldLastEvent
import org.lwjgl.opengl.GL11
import java.awt.Color

object RenderUtils {

    private fun Position.renderString0(string: String?, offsetX: Int = 0, offsetY: Int = 0): Int {
        val display = "§f$string"
        GlStateManager.pushMatrix()
        transform()
        val minecraft = Minecraft.getMinecraft()
        val renderer = minecraft.renderManager.fontRenderer

        val x = offsetX
        val y = offsetY

        GlStateManager.translate(x + 1.0, y + 1.0, 0.0)
        renderer.drawStringWithShadow(display, 0f, 0f, 0)


        GlStateManager.popMatrix()

        return renderer.getStringWidth(display)
    }

    fun Position.renderStrings(list: List<String>, extraSpace: Int = 0, posLabel: String) {
        if (list.isEmpty()) return

        var offsetY = 0
        var longestX = 0
        for (s in list) {
            val x = renderString0(s, offsetY = offsetY)
            if (x > longestX) {
                longestX = x
            }
            offsetY += 10 + extraSpace
        }
        GuiEditManager.add(this, posLabel, longestX, offsetY)
    }

    /**
     * Accepts a list of lines to print.
     * Each line is a list of things to print. Can print String or ItemStack objects.
     */
    fun Position.renderStringsAndItems(
        list: List<List<Any?>>,
        extraSpace: Int = 0,
        itemScale: Double = 1.0,
        posLabel: String,
    ) {
        if (list.isEmpty()) return

        var offsetY = 0
        var longestX = 0
        try {
            for (line in list) {
                val x = renderLine(line, offsetY, itemScale)
                if (x > longestX) {
                    longestX = x
                }
                offsetY += 10 + extraSpace + 2
            }
        } catch (e: NullPointerException) {
            println(" ")
            for (innerList in list) {
                println("new inner list:")
                for (any in innerList) {
                    println("any: '$any'")
                }
            }
            e.printStackTrace()
            LorenzUtils.debug("NPE in renderStringsAndItems!")
        }
        GuiEditManager.add(this, posLabel, longestX, offsetY)
    }

    /**
     * Accepts a single line to print.
     * This  line is a list of things to print. Can print String or ItemStack objects.
     */
    fun Position.renderSingleLineWithItems(list: List<Any?>, itemScale: Double = 1.0, posLabel: String) {
        if (list.isEmpty()) return
        val longestX = renderLine(list, 0, itemScale)
        GuiEditManager.add(this, posLabel, longestX, 10)
    }

    private fun Position.renderLine(line: List<Any?>, offsetY: Int, itemScale: Double = 1.0): Int {
        GlStateManager.pushMatrix()
        val mp = transform()
        GlStateManager.translate(0f, offsetY.toFloat(), 0F)
        var offsetX = 0
        Renderable.withMousePosition(mp.first, mp.second) {
            for (any in line) {
                val renderable = Renderable.fromAny(any, itemScale = itemScale)
                    ?: throw RuntimeException("Unknown render object: $any")
                renderable.render(offsetX, offsetY)
                offsetX += renderable.width
                GlStateManager.translate(renderable.width.toFloat(), 0F, 0F)
            }
        }
        GlStateManager.popMatrix()
        return offsetX
    }

    infix fun Slot.highlight(color: LorenzColor) {
        highlight(color.toColor())
    }

    infix fun Slot.highlight(color: Color) {
        val lightingState = GL11.glIsEnabled(GL11.GL_LIGHTING)

        GlStateManager.disableLighting()
        GlStateManager.color(1f, 1f, 1f, 1f)

        GlStateManager.pushMatrix()
        // TODO don't use z
        GlStateManager.translate(0f, 0f, 110 + Minecraft.getMinecraft().renderItem.zLevel)
        Gui.drawRect(
            this.xDisplayPosition,
            this.yDisplayPosition,
            this.xDisplayPosition + 16,
            this.yDisplayPosition + 16,
            color.rgb
        )
        GlStateManager.popMatrix()

        if (lightingState) GlStateManager.enableLighting()
    }

    fun Position.transform(): Pair<Int, Int> {
        GlStateManager.translate(getAbsX().toFloat(), getAbsY().toFloat(), 0F)
        GlStateManager.scale(effectiveScale, effectiveScale, 1F)
        val x = ((Utils.getMouseX() - getAbsX()) / effectiveScale).toInt()
        val y = ((Utils.getMouseY() - getAbsY()) / effectiveScale).toInt()
        return x to y
    }

    fun RenderWorldLastEvent.exactLocation(entity: Entity) = exactLocation(entity, partialTicks)

    fun RenderWorldLastEvent.exactPlayerEyeLocation(): LorenzVec {
        val player = Minecraft.getMinecraft().thePlayer
        val add = if (player.isSneaking) LorenzVec(0.0, 1.54, 0.0) else LorenzVec(0.0, 1.62, 0.0)
        return exactLocation(player).add(add)
    }

    fun exactLocation(entity: Entity, partialTicks: Float): LorenzVec {
        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks
        return LorenzVec(x, y, z)
    }
}