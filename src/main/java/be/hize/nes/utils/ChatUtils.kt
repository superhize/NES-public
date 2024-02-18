package be.hize.nes.utils

import net.minecraft.client.Minecraft
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.IChatComponent
import java.util.*

enum class MessageMode(val color: EnumChatFormatting) {
    INFO(EnumChatFormatting.AQUA), ERROR(EnumChatFormatting.RED), FATAL(EnumChatFormatting.DARK_RED);

    fun format(comp: IChatComponent): IChatComponent {
        val b = ChatComponentText("§e[OnlyFarm] §r")
        b.chatStyle.color = color
        b.appendSibling(comp)
        return b
    }
}

object CommandActionRegistry : CommandBase() {
    override fun getCommandName(): String = "__ofactioncallback"

    override fun getCommandUsage(sender: ICommandSender?): String = "Do not directly use this"
    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true

    data class ActionCallback(val callable: () -> Unit, val once: Boolean)

    val callbacks: MutableMap<String, ActionCallback> = mutableMapOf()

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        val callback = args.singleOrNull()
        if (callback == null) {
            showFail(sender)
            return
        }
        val ac = synchronized(callbacks) {
            val actionCallback = callbacks[callback]
            if (actionCallback == null) {
                showFail(sender)
                return
            }
            if (actionCallback.once)
                callbacks.remove(callback)
            actionCallback
        }
        ac.callable()
    }

    fun registerCallback(callable: () -> Unit, once: Boolean): String {
        val name = UUID.randomUUID().toString()
        val actionCallback = ActionCallback(callable, once)
        synchronized(callbacks) {
            callbacks[name] = actionCallback
        }
        return "/$commandName $name"
    }

    private fun showFail(sender: ICommandSender) {
        sender.showMessage(MessageMode.ERROR) {
            text("Misuse of internal callback command.")
        }
    }
}

class MessageTarget {
    val aggregate = mutableListOf<IChatComponent>()

    fun s(count: Int): String {
        return if (count == 1) "" else "s"
    }

    fun text(string: String): ChatComponentText {
        return component(ChatComponentText(string))
    }

    fun <T : IChatComponent> T.clickable(hover: String, once: Boolean = true, action: () -> Unit): T {
        chatStyle.chatHoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(hover))
        chatStyle.chatClickEvent =
            ClickEvent(ClickEvent.Action.RUN_COMMAND, CommandActionRegistry.registerCallback(action, once))
        return this
    }

    fun <T : IChatComponent> component(comp: T): T {
        aggregate.add(comp)
        return comp
    }

}

fun showPlayerMessage(mode: MessageMode = MessageMode.INFO, block: MessageTarget.() -> Unit) {
    Minecraft.getMinecraft().thePlayer?.showMessage(mode, block)
}

fun ICommandSender.showMessage(mode: MessageMode = MessageMode.INFO, block: MessageTarget.() -> Unit) {
    MessageTarget().also(block).aggregate.map { mode.format(it) }.forEach { addChatMessage(it) }
}
