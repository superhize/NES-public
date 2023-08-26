package be.hize.nes.features.misc

import at.hannibal2.skyhanni.data.ChatManager
import at.hannibal2.skyhanni.utils.IdentityCharacteristics
import be.hize.nes.NES
import be.hize.nes.features.chat.ChatGui
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RawChatMessage {

    private val messageHistory =
        object : LinkedHashMap<IdentityCharacteristics<IChatComponent>, ChatManager.MessageFilteringResult>() {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<IdentityCharacteristics<IChatComponent>, ChatManager.MessageFilteringResult>?): Boolean {
                return size > 100
            }
        }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return
        val original = event.message
        val key = IdentityCharacteristics(original)
        messageHistory[key] =
            ChatManager.MessageFilteringResult(original, ChatManager.ActionKind.ALLOWED, null, null)
    }

    private fun getRecentMessageHistory(): List<ChatManager.MessageFilteringResult> =
        messageHistory.toList().map { it.second }

    fun openGui() {
        NES.screenToOpen = ChatGui(getRecentMessageHistory())
    }
}