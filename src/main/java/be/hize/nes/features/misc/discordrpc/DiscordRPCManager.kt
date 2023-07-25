package be.hize.nes.features.misc.discordrpc

import at.hannibal2.skyhanni.utils.LorenzUtils
import be.hize.nes.NES
import be.hize.nes.events.ConfigLoadEvent
import be.hize.nes.utils.NESUtils
import com.google.gson.JsonObject
import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.IPCListener
import com.jagrosh.discordipc.entities.RichPresence
import kotlinx.coroutines.launch
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object DiscordRPCManager : IPCListener {
    private const val applicationID = 1096823765469499474L
    private const val updatePeriod = 1000L

    private val config get() = NES.feature.misc.discordRPC

    private var client: IPCClient? = null
    private lateinit var secondLine: DiscordStatus
    private lateinit var firstLine: DiscordStatus
    private var startTimestamp: Long? = null
    private var startOnce = false

    private var updateTimer: Timer? = null
    private var connected = false

    private val DiscordLocationKey = DiscordLocationKey()

    fun start(fromCommand: Boolean = false) {
        NES.coroutineScope.launch {
            try {
                if (isActive()) {
                    return@launch
                }
                NES.consoleLog("Starting Discord RPC...")

                firstLine = getStatusByConfigId(config.firstLine.get())
                secondLine = getStatusByConfigId(config.secondLine.get())
                startTimestamp = System.currentTimeMillis()
                client = IPCClient(applicationID)
                client?.setListener(this@DiscordRPCManager)

                try {
                    client?.connect()
                    if (fromCommand) LorenzUtils.chat("§a[NES] Successfully started Rich Presence!") // confirm that /shrpcstart worked
                } catch (ex: Exception) {
                    NES.consoleLog("Warn: Failed to connect to RPC!")
                    NES.consoleLog(ex.toString())
                    LorenzUtils.clickableChat("§e[NES] Discord Rich Presence was unable to start! " +
                        "This usually happens when you join SkyBlock when Discord is not started. " +
                        "Please run /shrpcstart to retry once you have launched Discord.", "shrpcstart")
                }
            } catch (ex: Throwable) {
                NES.consoleLog("Warn: Discord RPC has thrown an unexpected error while trying to start...")
                NES.consoleLog(ex.toString())
            }
        }
    }

    private fun stop() {
        NES.coroutineScope.launch {
            if (isActive()) {
                connected = false
                client?.close()
                startOnce = false
            }
        }
    }

    private fun isActive() = client != null && connected

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        NESUtils.onToggle(config.firstLine,
            config.secondLine,
            config.customFirstLine,
            config.customSecondLine) {
            if (isActive()) {
                updatePresence()
            }
        }
        config.enabled.whenChanged { _, new ->
            if (new) {
//                start()
            } else {
                stop()
            }
        }
    }
    fun updatePresence() {
        val location = DiscordStatus.LOCATION.getDisplayString()
        val discordIconKey = DiscordLocationKey.getDiscordIconKey(location)

        secondLine = getStatusByConfigId(config.secondLine.get())
        firstLine = getStatusByConfigId(config.firstLine.get())
        val presence: RichPresence = RichPresence.Builder()
            .setDetails(firstLine.getDisplayString())
            .setState(secondLine.getDisplayString())
            .setStartTimestamp(startTimestamp!!)
            .setLargeImage(discordIconKey, location)
            .build()
        client?.sendRichPresence(presence)
    }

    override fun onReady(client: IPCClient) {
        NES.consoleLog("Discord RPC Started.")
        connected = true
        updateTimer = Timer()
        updateTimer?.schedule(object : TimerTask() {
            override fun run() {
                updatePresence()
            }
        }, 0, updatePeriod)
    }

    override fun onClose(client: IPCClient, json: JsonObject?) {
        NES.consoleLog("Discord RPC closed.")
        this.client = null
        connected = false
        cancelTimer()
    }

    override fun onDisconnect(client: IPCClient?, t: Throwable?) {
        NES.consoleLog("Discord RPC disconnected.")
        this.client = null
        connected = false
        cancelTimer()
    }

    private fun cancelTimer() {
        updateTimer?.let {
            it.cancel()
            updateTimer = null
        }
    }

    private fun getStatusByConfigId(id: Int) = DiscordStatus.values().getOrElse(id) { DiscordStatus.NONE }

    private fun isEnabled() = config.enabled.get()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (startOnce || !isEnabled()) return
        if (LorenzUtils.inSkyBlock) {
            start()
            startOnce = true
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
        executor.schedule(
            {
                if (!LorenzUtils.inSkyBlock) {
                    stop()
                }
            },
            5,
            TimeUnit.SECONDS
        )
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        stop()
    }

    fun startCommand() {
        if (!config.enabled.get()) {
            LorenzUtils.chat("§c[NES] Discord Rich Presence is disabled. Enable it in the config §e/sh discord")
            return
        }

        if (isActive()) {
            LorenzUtils.chat("§e[NES] Discord Rich Presence is already active!")
            return
        }

        LorenzUtils.chat("§e[NES] Attempting to start Discord Rich Presence...")
        try {
            start(true)
        } catch (e: Exception) {
            LorenzUtils.chat("§c[NES] Unable to start Discord Rich Presence!")
        }
    }
}