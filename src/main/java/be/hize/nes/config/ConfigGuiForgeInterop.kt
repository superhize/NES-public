package be.hize.nes.config

import io.github.moulberry.moulconfig.gui.GuiScreenElementWrapper
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.fml.client.IModGuiFactory
import org.lwjgl.input.Keyboard
import java.io.IOException

@Suppress("unused")
class ConfigGuiForgeInterop : IModGuiFactory {
    override fun initialize(minecraft: Minecraft) {}
    override fun mainConfigGuiClass(): Class<out GuiScreen> {
        return WrappedSkyHanniConfig::class.java
    }

    override fun runtimeGuiCategories(): Set<IModGuiFactory.RuntimeOptionCategoryElement>? = null

    override fun getHandlerFor(runtimeOptionCategoryElement: IModGuiFactory.RuntimeOptionCategoryElement): IModGuiFactory.RuntimeOptionGuiHandler? =
        null

    class WrappedSkyHanniConfig(private val parent: GuiScreen) : GuiScreenElementWrapper(ConfigGuiManager.configEditor) {
        @Throws(IOException::class)
        override fun handleKeyboardInput() {
            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                Minecraft.getMinecraft().displayGuiScreen(parent)
                return
            }
            super.handleKeyboardInput()
        }
    }
}