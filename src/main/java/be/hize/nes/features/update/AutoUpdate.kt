package be.hize.nes.features.update

import be.hize.nes.NES
import be.hize.nes.utils.showMessage
import io.github.moulberry.notenoughupdates.util.MinecraftExecutor
import moe.nea.libautoupdate.UpdateContext
import moe.nea.libautoupdate.UpdateSource
import moe.nea.libautoupdate.UpdateTarget
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.CompletableFuture

object AutoUpdate {
    val updater = UpdateContext(
        UpdateSource.mavenSource("https://repo.hize.be/releases", "be.hize", "nes"),
        UpdateTarget.deleteAndSaveInTheSameFolder(AutoUpdate::class.java),
        SemanticVersion.fromString(NES.version2)!!,
        NES.MODID
    )

    init {
        updater.cleanup()
    }

    var potentialUpdate = updater.checkUpdate("")
    var shouldNotify = NES.feature.about.autoUpdates
    var notifyNonUpdate = false

    @SubscribeEvent
    fun onFirstPlayerInteraction(event: TickEvent.ClientTickEvent) {
        if (!shouldNotify) return
        if (event.phase != TickEvent.Phase.START) return
        val p = Minecraft.getMinecraft().thePlayer ?: return
        val update = potentialUpdate.getNow(null) ?: return
        shouldNotify = false
        if (!update.isUpdateAvailable && notifyNonUpdate) {
            p.showMessage {
                text("No update found.")
            }
        }
        if (update.isUpdateAvailable)
            p.showMessage {
                text("§eUpdate found. §8${update.context.currentVersion.display()}➜§3${update.update.versionName}§e. Click to update at next restart.")
                    .clickable("Click to queue the update at your next restart") {
                        p.showMessage { text("§eDownloading update ${update.update.versionName}") }
                        CompletableFuture.supplyAsync { update.prepareUpdate() }.thenAcceptAsync({
                            update.executePreparedUpdate()
                            p.showMessage { text("§eUpdate downloaded and queued for your next restart.") }
                        }, MinecraftExecutor.OnThread)
                    }
            }
    }

    fun onCommand() {
        potentialUpdate = updater.checkUpdate("")
        shouldNotify = true
        notifyNonUpdate = true
    }
}
