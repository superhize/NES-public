package be.hize.nes.features.chat

import at.hannibal2.skyhanni.data.ChatManager
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.OSUtils
import io.github.moulberry.moulconfig.internal.GlScissorStack
import io.github.moulberry.moulconfig.internal.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiUtilRenderComponents
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.IChatComponent
import org.lwjgl.input.Mouse

/**
 * @link https://github.com/romangraef
 * @author: nea89
**/
class ChatGui(private val history: List<ChatManager.MessageFilteringResult>) : GuiScreen() {
    private var scroll = -1.0
    private val w = 500
    private var wasMouseButtonDown = false
    private val h = 300
    private val reasonMaxLength =
        history.maxOf { it.actionReason?.let { Minecraft.getMinecraft().fontRendererObj.getStringWidth(it) } ?: 0 }
    private val historySize by lazy {
        history.sumOf { splitLine(it.message).size * 10 + 0 }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        drawDefaultBackground()
        GlStateManager.pushMatrix()
        val l = (width / 2.0 - w / 2.0).toInt()
        val t = (height / 2.0 - h / 2.0).toInt()
        GlStateManager.translate(l + 0.0, t + 0.0, 0.0)
        RenderUtils.drawFloatingRectDark(0, 0, w, h)
        GlStateManager.translate(5.0, 5.0 - scroll, 0.0)
        var mouseX = mouseX - l
        val isMouseButtonDown = if (mouseX in 0..w && mouseY in t..(t + h)) Mouse.isButtonDown(0) else false
        var mouseY = mouseY - (t - scroll).toInt()
        val sr = ScaledResolution(mc)
        GlScissorStack.push(l + 5, t + 5, w + l - 5, h + t - 5, sr)

        for (msg in history) {
            drawString(mc.fontRendererObj, msg.actionKind.renderedString, 0, 0, -1)
            drawString(mc.fontRendererObj, msg.actionReason, ChatManager.ActionKind.maxLength + 5, 0, -1)
            val size = drawMultiLineText(
                msg.message,
                ChatManager.ActionKind.maxLength + reasonMaxLength + 10,
            )
            if (mouseX in 0..w && mouseY in 0..(size * 10) && (isMouseButtonDown && !wasMouseButtonDown)) {
                if (LorenzUtils.isShiftKeyDown()) {
                    OSUtils.copyToClipboard(IChatComponent.Serializer.componentToJson(msg.message))
                    LorenzUtils.chat("Copied structured chat line to clipboard")
                } else {
                    OSUtils.copyToClipboard(msg.message.formattedText)
                    LorenzUtils.chat("Copied chat line to clipboard")
                }
            }
            mouseY -= size * 10
        }
        GlScissorStack.pop(sr)
        wasMouseButtonDown = isMouseButtonDown
        GlStateManager.popMatrix()
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    fun splitLine(comp: IChatComponent): List<IChatComponent> {
        return GuiUtilRenderComponents.splitText(
            comp,
            w - (ChatManager.ActionKind.maxLength + reasonMaxLength + 10 + 10),
            mc.fontRendererObj,
            false,
            true
        )
    }

    override fun initGui() {
        super.initGui()
        if (this.scroll < 0) {
            setScroll(1000000000.0)
        }
    }

    fun setScroll(newScroll: Double) {
        this.scroll = newScroll.coerceAtMost(historySize - h + 10.0).coerceAtLeast(0.0)
    }

    fun drawMultiLineText(comp: IChatComponent, xPos: Int): Int {
        val modifiedSplitText = splitLine(comp)
        for (line in modifiedSplitText) {
            drawString(
                mc.fontRendererObj,
                line.formattedText,
                xPos,
                0,
                -1
            )
            GlStateManager.translate(0F, 10F, 0F)
        }
        return modifiedSplitText.size
    }


    override fun handleMouseInput() {
        super.handleMouseInput()
        setScroll(scroll - Mouse.getEventDWheel())
    }

}