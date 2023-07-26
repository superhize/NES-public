package be.hize.nes.mixin.hooks

import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SpecialColour
import be.hize.nes.NES
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO

var updated = false
val default = ResourceLocation("textures/entity/creeper/creeper_armor.png")
val custom = ResourceLocation("nes", "creeper_armor.png")
fun modifyChargedCreeperLayer(res: ResourceLocation): ResourceLocation {
    @Suppress("NAME_SHADOWING")
    val res = res
    return if (LorenzUtils.inSkyBlock && NES.feature.misc.ghost.recolorCreeper.get()) {
        if (!updated) {
            updated = true
            fillImageWithColor(res, Color(SpecialColour.specialToChromaRGB(NES.feature.misc.ghost.creeperColor.get())), custom)
            custom
        } else {
            custom
        }
    } else {
        default
    }
}

fun fillImageWithColor(resourceLocation: ResourceLocation, color: Color, outputResourceLocation: ResourceLocation) {
    try {
        val minecraft = Minecraft.getMinecraft()
        val inputStream = minecraft.resourceManager.getResource(resourceLocation).inputStream
        val image = ImageIO.read(inputStream)

        val width = image.width
        val height = image.height
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        val graphics = bufferedImage.createGraphics()
        graphics.color = color
        graphics.fillRect(0, 0, width, height)
        graphics.dispose()


        minecraft.textureManager.loadTexture(outputResourceLocation, DynamicTexture(bufferedImage))
    } catch (e: IOException) {
        e.printStackTrace()
    }
}