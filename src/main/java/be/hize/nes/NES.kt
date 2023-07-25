package be.hize.nes

import be.hize.nes.config.ConfigManager
import be.hize.nes.config.Features
import be.hize.nes.config.commands.Commands.init
import be.hize.nes.data.GuiEditManager
import be.hize.nes.data.ProfileStorageData
import be.hize.nes.data.RenderGuiData
import be.hize.nes.features.inventory.ChestValue
import be.hize.nes.features.misc.*
import be.hize.nes.features.misc.coordinate.ShowCoordinate
import be.hize.nes.features.misc.discordrpc.DiscordRPCManager
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(
    modid = NES.MODID,
    clientSideOnly = true,
    useMetadata = true,
    guiFactory = "be.hize.nes.config.ConfigGuiForgeInterop",
    version = "0.1.Beta.1",
    name = "NotEnoughSkyhanni")
internal class NES {

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        loadModule(this)
        loadModule(ProfileStorageData())
        loadModule(GuiEditManager())
        loadModule(RenderGuiData())
        loadModule(ShowFPS())
        loadModule(ShowCoordinate())
        loadModule(Facing())
        loadModule(DiscordRPCManager)
        loadModule(Trapper())
        loadModule(Ghost())
        loadModule(ButtonOnPause())
        loadModule(ChestValue())

        init()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        configManager = ConfigManager()
        configManager.firstLoad()
        Runtime.getRuntime().addShutdownHook(Thread { configManager.saveConfig("shutdown-hook") })
    }

    private fun loadModule(obj: Any) {
        modules.add(obj)
        MinecraftForge.EVENT_BUS.register(obj)
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent?) {
        if (screenToOpen != null) {
            screenTicks++
            if (screenTicks == 5) {
                Minecraft.getMinecraft().displayGuiScreen(screenToOpen)
                screenTicks = 0
                screenToOpen = null
            }
        }
    }

    companion object {
        const val MODID = "nes"

        @JvmStatic
        val version: String
            get() = Loader.instance().indexedModList[MODID]!!.version

        @JvmStatic
        val feature: Features get() = configManager.features
        lateinit var configManager: ConfigManager
        val logger: Logger = LogManager.getLogger("NES")
        fun getLogger(name: String): Logger {
            return LogManager.getLogger("NES.$name")
        }

        val modules: MutableList<Any> = ArrayList()
        val globalJob: Job = Job(null)
        val coroutineScope = CoroutineScope(
            CoroutineName("NES") + SupervisorJob(globalJob)
        )
        var screenToOpen: GuiScreen? = null
        private var screenTicks = 0
        fun consoleLog(message: String) {
            logger.log(Level.INFO, message)
        }
    }
}
