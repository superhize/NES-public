package be.hize.nes.features.misc.waypoint

import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.test.GriffinUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.chat
import at.hannibal2.skyhanni.utils.LorenzUtils.clickableChat
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RenderUtils.drawColor
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.StringUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import be.hize.nes.NES
import be.hize.nes.features.misc.waypoint.Waypoint.Command.addWaypoint
import be.hize.nes.utils.NESLogger
import net.minecraft.client.Minecraft
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.concurrent.fixedRateTimer
import kotlin.math.roundToInt

object Waypoint {

    private val logger = NESLogger("misc/waypoint")
    private var waypoints = mutableMapOf<String, Waypoints>()
    private val patcherPattern = "(?<playerName>.*): x: (?<x>.*), y: (?<y>.*), z: (?<z>.*)".toPattern()

    @SubscribeEvent
    fun onPatcherCoordinates(event: LorenzChatEvent) {
        if (!isEnabled()) return
        val message = event.message.removeColor()
        patcherPattern.matchMatcher(message) {
            var description = group("playerName").split(" ").last()
            val x = group("x").toInt()
            val y = group("y").toInt()

            val end = group("z")
            val z = if (end.contains(" ")) {
                val split = end.split(" ")
                val extra = split.drop(1).joinToString(" ")
                description += " $extra"

                split.first().toInt()
            } else end.toInt()
            addWaypoint(
                x.toDouble(),
                y.toDouble(),
                z.toDouble(),
                LorenzColor.GREEN,
                "§a${description.replace(" ", "")}",
                "patcher"
            )
            logger.log("got patcher coords and username")
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onWorldRender(event: LorenzRenderWorldEvent) {
        if (!isEnabled()) return
        if (waypoints.isEmpty()) return
        for ((tag, waypoint) in waypoints) {
            event.drawColor(waypoint.location, waypoint.color, alpha = 1f)
            event.drawWaypointFilled(
                waypoint.location,
                waypoint.color.toColor(),
                seeThroughBlocks = true,
                beacon = true
            )
            event.drawDynamicText(
                waypoint.location,
                tag.replace("&", "§"),
                1.5,
            )
            /*val distance = waypoint.location.distanceToPlayer().roundToInt()
            event.drawDynamicText(
                waypoint.location.add(0, 1, 0),
                "$distance blocs".replace("&", "§"),
                1.5,
            )*/
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        waypoints.clear()
    }

    data class Waypoints(
        val location: LorenzVec,
        val color: LorenzColor,
        val tag: String,
        val group: String
    )

    private fun isEnabled() = LorenzUtils.inSkyBlock && NES.feature.misc.enableWaypoints

    object Command {

        private var currentPage = 1

        private val colorMap = mapOf(
            "black" to LorenzColor.BLACK,
            "darkblue" to LorenzColor.DARK_BLUE,
            "darkgreen" to LorenzColor.DARK_GREEN,
            "darkaqua" to LorenzColor.DARK_AQUA,
            "darkred" to LorenzColor.DARK_RED,
            "darkpurple" to LorenzColor.DARK_PURPLE,
            "gold" to LorenzColor.GOLD,
            "gray" to LorenzColor.GRAY,
            "darkgray" to LorenzColor.DARK_GRAY,
            "blue" to LorenzColor.BLUE,
            "green" to LorenzColor.GREEN,
            "aqua" to LorenzColor.AQUA,
            "red" to LorenzColor.RED,
            "lightpurple" to LorenzColor.LIGHT_PURPLE,
            "yellow" to LorenzColor.YELLOW,
            "white" to LorenzColor.WHITE
        )

        private fun help() {
            chat(" === Waypoints === ")
            chat("§6/neswaypoint help - show this")
            chat("§6/neswaypoint add <x> <y> <z> <color> <tag> - Add a waypoint")
            chat("§6/neswaypoint add <tag> - Add a waypoint at your position")
            chat("§6/neswaypoint add <tag> <color> - Add a waypoint at your position with given color")
            chat("§6/neswaypoint remove <tag> - Remove a waypoint")
            chat("§6/neswaypoint clear - clear all waypoints")
            chat("§6/neswaypoint list - list all waypoints")
            chat(" === === === === ")
        }

        fun process(args: Array<String>) {
            if (args.isEmpty()) help()

            val param = args.drop(1)
            when (args[0]) {
                "add" -> {
                    if (param.size == 1) {
                        val x = Minecraft.getMinecraft().thePlayer.posX
                        val y = Minecraft.getMinecraft().thePlayer.posY
                        val z = Minecraft.getMinecraft().thePlayer.posZ
                        val tag = param[0]
                        addWaypoint(x, y, z, tag = tag)
                    }
                    if (param.size == 2) {
                        val x = Minecraft.getMinecraft().thePlayer.posX
                        val y = Minecraft.getMinecraft().thePlayer.posY
                        val z = Minecraft.getMinecraft().thePlayer.posZ
                        val tag = param[0]
                        val colorName = param[1]
                        if (!colorMap.contains(colorName)) {
                            chat("§e[NES] §cInvalid color! Valid colors are: §b ${colorMap.keys.joinToString(" - ")}")
                            return
                        }
                        val color = colorMap.getOrDefault(colorName, LorenzColor.GREEN)
                        addWaypoint(x, y, z, tag = tag, color = color)
                    }
                    if (param.size == 4) {
                        val x = param[0].toDouble()
                        val y = param[1].toDouble()
                        val z = param[2].toDouble()
                        val tag = param[3]
                        addWaypoint(x, y, z, tag = tag)
                    }
                    if (param.size == 5) {
                        val x = param[0].toDouble()
                        val y = param[1].toDouble()
                        val z = param[2].toDouble()
                        val tag = param[3]
                        val group = param[4]
                        val color = LorenzColor.GREEN
                        addWaypoint(x, y, z, tag = tag, color = color, group = group)
                    }
                }

                "remove" -> {
                    if (param.size == 1) {
                        deleteWaypoint(param[0])
                        displayWaypointsPage(currentPage)
                    }
                }

                "clear" -> {
                    waypoints.clear()
                    chat("§e[NES] §aWaypoints cleared.")
                }

                "list" -> {
                    currentPage = if (param.isEmpty()) {
                        displayWaypointsPage(1)
                        1
                    } else {
                        val page = param[0].toInt()
                        displayWaypointsPage(page)
                        page
                    }
                }

                "share" -> {
                    if (param.size == 1) {
                        val player = Minecraft.getMinecraft().thePlayer
                        val group = param[0]
                        val message = mutableListOf<String>()
                        for ((t, w) in waypoints) {
                            if (w.group == group) {
                                val l = w.location
                                message.add("${t.removeColor()}: ${l.x.toInt()}, ${l.y.toInt()}, ${l.z.toInt()}")
                            }
                        }
                        player.sendChatMessage(message.joinToString(" | "))
                    }
                }

                else -> {
                    help()
                }
            }

        }

        fun addWaypoint(
            x: Double,
            y: Double,
            z: Double,
            color: LorenzColor = LorenzColor.GREEN,
            tag: String,
            group: String = "None"
        ) {
            waypoints[tag] = Waypoints(LorenzVec(x, y, z), color, tag, group)
            chat("§e[NES] §aAdded waypoint!")
        }

        private fun deleteWaypoint(check: String) {
            if (waypoints.contains(check)) {
                waypoints.remove(check)
                chat("§e[NES] §cRemoved waypoint!")
            }

            for ((tag, _) in waypoints) {
                val newTag = tag.replace("&", "§").removeColor()
                if (newTag == check) {
                    waypoints.remove(tag)
                    chat("§e[NES] §cRemoved waypoint!")
                }
            }
        }

        private fun displayWaypointsPage(pageNumber: Int) {
            val waypointsPerPage = 10
            val startIndex = (pageNumber - 1) * waypointsPerPage
            val endIndex = startIndex + waypointsPerPage

            val sortedWaypoints = waypoints.values.toList().sortedBy { it.tag }
            val totalPages = (sortedWaypoints.size + waypointsPerPage - 1) / waypointsPerPage

            if (pageNumber < 1 || pageNumber > totalPages) {
                chat("Page $pageNumber out of range.")
                return
            }

            val waypointsToShow = sortedWaypoints.subList(startIndex, endIndex.coerceAtMost(sortedWaypoints.size))

            chat("=== Waypoints - Page $pageNumber/$totalPages ===")
            for (waypoint in waypointsToShow) {
                clickableChat(
                    "§6Tag: §b${waypoint.tag}§7: §6X§7: §b${waypoint.location.x.roundToInt()}§7, §6Y§7: §b${waypoint.location.y.roundToInt()}§7, §6Z§7: §b${waypoint.location.z.roundToInt()}§7," +
                        " §6Distance§7: §b${waypoint.location.distanceToPlayer().roundToInt()} §6blocs",
                    "/neswaypoint remove ${waypoint.tag}"
                )
            }


            buildNavigationMessage(pageNumber, totalPages)
        }

        private fun buildNavigationMessage(currentPage: Int, totalPages: Int) {
            val previousPage = if (currentPage > 1) currentPage - 1 else 1
            val nextPage = if (currentPage < totalPages) currentPage + 1 else totalPages

            val previous = ChatComponentText("← Previous Page")
            previous.chatStyle.chatClickEvent =
                ClickEvent(ClickEvent.Action.RUN_COMMAND, "/neswaypoint list $previousPage")
            previous.chatStyle.chatHoverEvent =
                HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("§eExecute /neswaypoint list $previousPage"))

            val next = ChatComponentText("Next Page →")
            next.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/neswaypoint list $nextPage")
            next.chatStyle.chatHoverEvent =
                HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("§eExecute /neswaypoint list $nextPage"))

            val arrow = ChatComponentText("").appendSibling(previous)
            arrow.appendText(" - ").appendSibling(next)

            Minecraft.getMinecraft().thePlayer.addChatMessage(arrow)
        }
    }
}