package be.hize.nes.data


import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.HypixelJoinEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import be.hize.nes.NES
import be.hize.nes.config.Storage
import be.hize.nes.events.ConfigLoadEvent
import be.hize.nes.events.PreProfileSwitchEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent


object ProfileStorageData {
    var playerSpecific: Storage.PlayerSpecific? = null
    var profileSpecific: Storage.ProfileSpecific? = null
    var loaded = false
    var noTabListTime = -1L
    private var nextProfile: String? = null

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChat(event: LorenzChatEvent) {
        "§7Switching to profile (?<name>.*)\\.\\.\\.".toPattern().matchMatcher(event.message) {
            nextProfile = group("name").lowercase()
            loaded = false
            PreProfileSwitchEvent().postAndCatch()
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onWorldChange(event: WorldEvent.Load) {
        val profileName = nextProfile ?: return
        nextProfile = null

        val playerSpecific = playerSpecific
        if (playerSpecific == null) {
            LorenzUtils.error("profileSpecific after profile swap can not be set: playerSpecific is null!")
            return
        }
        loadProfileSpecific(playerSpecific, profileName, "profile swap (chat message)")
        ConfigLoadEvent().postAndCatch()
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onProfileJoin(event: ProfileJoinEvent) {
        val playerSpecific = playerSpecific
        if (playerSpecific == null) {
            LorenzUtils.error("playerSpecific is null in ProfileJoinEvent!")
            return
        }

        if (profileSpecific == null) {
            val profileName = event.name
            loadProfileSpecific(playerSpecific, profileName, "first join (chat message)")
        }
    }

    @SubscribeEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        if (profileSpecific != null) return
        val playerSpecific = playerSpecific ?: return
        for (line in event.tabList) {
            val pattern = "§e§lProfile: §r§a(?<name>.*)".toPattern()
            pattern.matchMatcher(line) {
                val profileName = group("name").lowercase()
                loadProfileSpecific(playerSpecific, profileName, "tab list")
                nextProfile = null
                return
            }
        }

        if (LorenzUtils.inSkyBlock) {
            noTabListTime = System.currentTimeMillis()
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (!LorenzUtils.inSkyBlock) return
        if (noTabListTime == -1L) return

        if (System.currentTimeMillis() > noTabListTime + 3_000) {
            noTabListTime = System.currentTimeMillis()
            LorenzUtils.chat(
                "§c[NES] Extra Information from Tab list not found! " +
                    "Enable it: SkyBlock Menu ➜ Settings ➜ Personal ➜ User Interface ➜ Player List Info"
            )
        }
    }

    private fun loadProfileSpecific(playerSpecific: Storage.PlayerSpecific, profileName: String, reason: String) {
        noTabListTime = -1
        profileSpecific = playerSpecific.profiles.getOrPut(profileName) { Storage.ProfileSpecific() }
        ConfigLoadEvent().postAndCatch()
        ProfileStorageData.loaded = true
    }

    @SubscribeEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        val playerUuid = LorenzUtils.getRawPlayerUuid()
        playerSpecific = NES.feature.storage.players.getOrPut(playerUuid) { Storage.PlayerSpecific() }
        ConfigLoadEvent().postAndCatch()
    }

}