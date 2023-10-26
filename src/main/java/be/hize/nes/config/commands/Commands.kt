package be.hize.nes.config.commands

import be.hize.nes.NES
import be.hize.nes.config.ConfigGuiManager
import be.hize.nes.data.GuiEditManager
import be.hize.nes.features.misc.RawChatMessage
import be.hize.nes.features.misc.waypoint.Waypoint
import be.hize.nes.features.misc.discordrpc.DiscordRPCManager
import net.minecraft.client.Minecraft
import net.minecraft.command.ICommandSender
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.ClientCommandHandler


object Commands {

    private val openMainMenu: (Array<String>) -> Unit = {
        if (it.isNotEmpty()) {
            if (it[0].lowercase() == "gui") {
                GuiEditManager.openGuiPositionEditor()
            } else {
                ConfigGuiManager.openConfigGui(it.joinToString(" "))
            }
        } else {
            ConfigGuiManager.openConfigGui()
        }
    }

    // command -> description
    private val commands = mutableListOf<CommandInfo>()

    enum class CommandCategory(val color: String, val categoryName: String, val description: String) {
        MAIN("§6", "Main Command", "Most useful commands of NES"),
        USERS_NORMAL("§e", "Normal Command", "Normal Command for everyone to use"),
        USERS_BUG_FIX("§f", "User Bug Fix", "A Command to fix small bugs"),
        DEVELOPER_CODING_HELP(
            "§5", "Developer Coding Help",
            "A Command that can help with developing new features. §cIntended for developers only!"
        ),
        DEVELOPER_DEBUG_FEATURES(
            "§9", "Developer Debug Features",
            "A Command that is useful for monitoring/debugging existing features. §cIntended for developers only!"
        ),
        INTERNAL("§8", "Internal Command", "A Command that should §cnever §7be called manually!"),
    }

    class CommandInfo(val name: String, val description: String, val category: Commands.CommandCategory)

    private var currentCategory = CommandCategory.MAIN

    fun init() {
        currentCategory = CommandCategory.MAIN
        usersMain()

        currentCategory = CommandCategory.USERS_NORMAL
        usersNormal()

        currentCategory = CommandCategory.USERS_BUG_FIX
        usersBugFix()

        currentCategory = CommandCategory.DEVELOPER_CODING_HELP
        developersCodingHelp()

        currentCategory = CommandCategory.DEVELOPER_DEBUG_FEATURES
        developersDebugFeatures()

        currentCategory = CommandCategory.INTERNAL
        internalCommands()
    }

    private fun usersMain() {
        registerCommand("nes", "Open the main menu", openMainMenu)
        registerCommand("nescommands", "Show this list") { commandHelp(it) }
    }

    private fun usersNormal() {
        registerCommand(
            "nesrpcstart",
            "Manually starts the Discord Rich Presence feature"
        ) { DiscordRPCManager.startCommand() }
        registerCommand("neswaypoint",
            "Waypoint commands"){
            Waypoint.Command.process(it)
        }
    }

    private fun usersBugFix() {}
    private fun developersCodingHelp() {
        registerCommand("neschathistory", "Show chat history") {
            RawChatMessage.openGui()
        }
    }

    private fun developersDebugFeatures() {
        registerCommand(
            "nesconfigsave",
            "Manually saving the config"
        ) { NES.configManager.saveConfig("manual-command") }
    }

    private fun internalCommands() {}

    private fun commandHelp(args: Array<String>) {
        var filter: (String) -> Boolean = { true }
        val title: String
        if (args.size == 1) {
            val searchTerm = args[0].lowercase()
            filter = { it.lowercase().contains(searchTerm) }
            title = "SkyHanni commands with '§e$searchTerm§7'"
        } else {
            title = "All SkyHanni commands"
        }
        val base = ChatComponentText(" \n§7$title:\n")
        for (command in commands) {
            if (!filter(command.name) && !filter(command.description)) continue
            val category = command.category
            val name = command.name
            val color = category.color
            val text = ChatComponentText("$color/$name")
            text.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/$name")

            val hoverText = buildList {
                add("§e/$name")
                add(" §7${command.description}")
                add("")
                add("$color${category.categoryName}")
                add("  §7${category.description}")
            }

            text.chatStyle.chatHoverEvent =
                HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(hoverText.joinToString("\n")))
            base.appendSibling(text)
            base.appendSibling(ChatComponentText("§7, "))
        }
        base.appendSibling(ChatComponentText("\n "))
        Minecraft.getMinecraft().thePlayer.addChatMessage(base)
    }

    private fun registerCommand(name: String, description: String, function: (Array<String>) -> Unit) {
        ClientCommandHandler.instance.registerCommand(SimpleCommand(name, createCommand(function)))
        commands.add(CommandInfo(name, description, currentCategory))
    }

    private fun createCommand(function: (Array<String>) -> Unit) = object : SimpleCommand.ProcessCommandRunnable() {
        override fun processCommand(sender: ICommandSender?, args: Array<String>?) {
            if (args != null) function(args.asList().toTypedArray())
        }
    }
}