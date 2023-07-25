package be.hize.nes.config.core.config.gui

import at.hannibal2.skyhanni.data.OtherInventoryData
import at.hannibal2.skyhanni.utils.GuiRenderUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import be.hize.nes.config.core.config.Position
import be.hize.nes.data.GuiEditManager.Companion.getAbsX
import be.hize.nes.data.GuiEditManager.Companion.getAbsY
import be.hize.nes.data.GuiEditManager.Companion.getDummySize
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.io.IOException

class GuiPositionEditor(private val positions: List<Position>, private val border: Int) : GuiScreen() {
    private var grabbedX = 0
    private var grabbedY = 0
    private var clickedPos = -1

    override fun onGuiClosed() {
        super.onGuiClosed()
        clickedPos = -1
        OtherInventoryData.close()
    }

    override fun drawScreen(unusedX: Int, unusedY: Int, partialTicks: Float) {
        super.drawScreen(unusedX, unusedY, partialTicks)
        drawDefaultBackground()

        val hoveredPos = renderRectangles()

        renderLabels(hoveredPos)
    }

    private fun renderLabels(hoveredPos: Int) {
        GuiRenderUtils.drawStringCentered("§cNES Position Editor", getScaledWidth() / 2, 8)

        var displayPos = -1
        if (clickedPos != -1) {
            if (positions[clickedPos].clicked) {
                displayPos = clickedPos
            }
        }
        if (displayPos == -1) {
            displayPos = hoveredPos
        }

        // When the mouse is not currently hovering over a gui element
        if (displayPos == -1) {
            GuiRenderUtils.drawStringCentered("§eTo edit hidden GUI elements set a key in /sh edit", getScaledWidth() / 2, 20)
            GuiRenderUtils.drawStringCentered("§ethen click that key while the GUI element is visible", getScaledWidth() / 2, 32)
            return
        }

        val pos = positions[displayPos]
        val location = "§7x: §e${pos.rawX}§7, y: §e${pos.rawY}"
        GuiRenderUtils.drawStringCentered("§b" + pos.internalName, getScaledWidth() / 2, 18)
        GuiRenderUtils.drawStringCentered(location, getScaledWidth() / 2, 28)
    }

    private fun renderRectangles(): Int {
        var hoveredPos = -1
        GlStateManager.pushMatrix()
        width = getScaledWidth()
        height = getScaledHeight()
        val mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth
        val mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1
        for ((index, position) in positions.withIndex()) {
            var elementWidth = position.getDummySize(true).x
            var elementHeight = position.getDummySize(true).y
            if (position.clicked) {
                grabbedX += position.moveX(mouseX - grabbedX, elementWidth)
                grabbedY += position.moveY(mouseY - grabbedY, elementHeight)
            }
            val x = position.getAbsX()
            val y = position.getAbsY()

            elementWidth = position.getDummySize().x
            elementHeight = position.getDummySize().y
            drawRect(x - border, y - border, x + elementWidth + border * 2, y + elementHeight + border * 2, -0x7fbfbfc0)

            if (GuiRenderUtils.isPointInRect(mouseX, mouseY, x - border, y - border, elementWidth + border * 2, elementHeight + border * 2)) {
                hoveredPos = index
            }
        }
        GlStateManager.popMatrix()
        return hoveredPos
    }

    private fun getScaledHeight() = ScaledResolution(Minecraft.getMinecraft()).scaledHeight
    private fun getScaledWidth() = ScaledResolution(Minecraft.getMinecraft()).scaledWidth

    @Throws(IOException::class)
    override fun mouseClicked(originalX: Int, priginalY: Int, mouseButton: Int) {
        super.mouseClicked(originalX, priginalY, mouseButton)

        if (mouseButton != 0) return

        val mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth
        val mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1
        for (i in positions.indices.reversed()) {
            val position = positions[i]
            val elementWidth = position.getDummySize().x
            val elementHeight = position.getDummySize().y
            val x = position.getAbsX()
            val y = position.getAbsY()
            if (!position.clicked) {
                if (GuiRenderUtils.isPointInRect(mouseX, mouseY, x - border, y - border, elementWidth + border * 2, elementHeight + border * 2)) {
                    clickedPos = i
                    position.clicked = true
                    grabbedX = mouseX
                    grabbedY = mouseY
                    break
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)

        if (clickedPos == -1) return
        val position = positions[clickedPos]
        if (position.clicked) return

        val dist = if (LorenzUtils.isShiftKeyDown()) 10 else 1
        val elementWidth = position.getDummySize(true).x
        val elementHeight = position.getDummySize(true).y
        if (keyCode == Keyboard.KEY_DOWN) {
            position.moveY(dist, elementHeight)
        } else if (keyCode == Keyboard.KEY_UP) {
            position.moveY(-dist, elementHeight)
        } else if (keyCode == Keyboard.KEY_LEFT) {
            position.moveX(-dist, elementWidth)
        } else if (keyCode == Keyboard.KEY_RIGHT) {
            position.moveX(dist, elementWidth)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)

        for (position in positions) {
            position.clicked = false
        }
    }

    override fun mouseClickMove(originalX: Int, priginalY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        super.mouseClickMove(originalX, priginalY, clickedMouseButton, timeSinceLastClick)

        for (position in positions) {
            if (!position.clicked) continue

            val mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth
            val mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1
            val elementWidth = position.getDummySize(true).x
            val elementHeight = position.getDummySize(true).y
            grabbedX += position.moveX(mouseX - grabbedX, elementWidth)
            grabbedY += position.moveY(mouseY - grabbedY, elementHeight)
        }
    }
}