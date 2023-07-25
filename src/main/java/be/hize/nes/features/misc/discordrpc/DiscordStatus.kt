package be.hize.nes.features.misc.discordrpc

import at.hannibal2.skyhanni.data.*
import at.hannibal2.skyhanni.data.GardenCropMilestones.Companion.getCounter
import at.hannibal2.skyhanni.data.GardenCropMilestones.Companion.progressToNextLevel
import at.hannibal2.skyhanni.features.garden.GardenAPI.getCropType
import at.hannibal2.skyhanni.features.rift.everywhere.RiftAPI
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TabListData
import io.github.moulberry.notenoughupdates.util.SkyBlockTime
import java.util.function.Supplier
import java.util.regex.Pattern

var lastKnownDisplayStrings: MutableMap<DiscordStatus, String> =
    mutableMapOf()

val purseRegex = Regex("""(?:Purse|Piggy): ([\d,]+)[\d.]*""")
val bitsRegex = Regex("""Bits: ([\d|,]+)[\d|.]*""")


private fun getVisitingName(): String {
    val tabData = TabListData.getTabList()
    val ownerRegex = Pattern.compile(".*Owner: (?<username>\\w+).*")
    for (line in tabData) {
        val colorlessLine = line.removeColor()
        val ownerMatcher = ownerRegex.matcher(colorlessLine)
        if (ownerMatcher.matches()) {
            return ownerMatcher.group("username")
        }
    }
    return "Someone"
}

enum class DiscordStatus(private val displayMessageSupplier: Supplier<String>?) {

    NONE(null),

    PURSE({
        val scoreboard = ScoreboardData.sidebarLinesFormatted
        // Matches coins amount in purse or piggy, with optional decimal points
        val coins = scoreboard.firstOrNull { purseRegex.matches(it.removeColor()) }?.let {
            purseRegex.find(it.removeColor())?.groupValues?.get(1) ?: ""
        }
        if (coins == "1") {
            lastKnownDisplayStrings[PURSE] = "1 Coin"
        } else if (coins != "" && coins != null) {
            lastKnownDisplayStrings[PURSE] = "$coins Coins"
        }
        lastKnownDisplayStrings[PURSE] ?: ""
    }),
    LOCATION({
        var location = LorenzUtils.skyBlockArea
        val island = LorenzUtils.skyBlockIsland
        if (location == "Your Island") location = "Private Island"
        if (island == IslandType.PRIVATE_ISLAND_GUEST) lastKnownDisplayStrings[LOCATION] =
            "${getVisitingName()}'s Island"
        else if (location != "None" && location != "invalid") {
            lastKnownDisplayStrings[LOCATION] = location
        }
        lastKnownDisplayStrings[LOCATION] ?: "None"
    }),

    BITS({
        val scoreboard = ScoreboardData.sidebarLinesFormatted
        val bits = scoreboard.firstOrNull { bitsRegex.matches(it.removeColor()) }?.let {
            bitsRegex.find(it.removeColor())?.groupValues?.get(1)
        }

        when (bits) {
            "1" -> "1 Bit"
            null -> "0 Bits"
            else -> "$bits Bits"
        }
    }),

    STATS({
        val groups = ActionBarStatsData.groups
        val statString = if (!RiftAPI.inRift()) {
            "❤${groups["health"]} ❈${groups["defense"]} ✎${groups["mana"]}"
        } else {
            "${groups["riftTime"]}ф ✎${groups["mana"]}"
        }
        if (groups["mana"] != "") {
            lastKnownDisplayStrings[STATS] = statString
        }
        lastKnownDisplayStrings[STATS] ?: ""
    }),

    ITEM({
        InventoryUtils.getItemInHand()?.let {
            String.format("Holding ${it.displayName.removeColor()}")
        } ?: "No item in hand"
    }),

    TIME({
        fun formatNum(num: Int): Int {
            val rem = num % 10
            var returnNum = num - rem // floor()
            if (returnNum == 0) {
                returnNum = "0$num".toInt()
            }
            return returnNum
        }

        val date: SkyBlockTime = SkyBlockTime.now()
        val hour = if (date.hour > 12) date.hour - 12 else date.hour
        val timeOfDay = if (date.hour > 11) "pm" else "am" // hooray for 12-hour clocks
        "${SkyBlockTime.monthName(date.month)} ${date.day}${SkyBlockTime.daySuffix(date.day)}, $hour:${formatNum(date.minute)}$timeOfDay" // Early Winter 1st, 12:00pm
    }),

    PROFILE({
        val player = LorenzUtils.getPlayerName()

        val tabData = TabListData.getTabList()
        val levelRegex = Regex("""\[(\d{1,3})] $player""")
        var sbLevel = ""
        for (line in tabData) {
            if (line.contains(player)) {
                val colorlessLine = line.removeColor()
                sbLevel = levelRegex.find(colorlessLine)!!.groupValues[1]
                break
            }
        }

        var profile = "SkyBlock Level: [$sbLevel] on "

        profile += (
            if (HypixelData.ironman) "♲"
            else if (HypixelData.bingo) "Ⓑ"
            else if (HypixelData.stranded) "☀"
            else ""
            )

        val fruit = HypixelData.profileName.firstLetterUppercase()
        if (fruit == "") profile =
            lastKnownDisplayStrings[PROFILE] ?: "SkyBlock Level: [$sbLevel]" // profile fruit has not loaded in yet
        else profile += fruit

        lastKnownDisplayStrings[PROFILE] = profile
        profile
    }),

    SLAYER({
        var slayerName = ""
        var slayerLevel = ""
        var bossAlive = "spawning"
        val slayerRegex =
            Pattern.compile("(?<name>(?:\\w| )*) (?<level>[IV]+)") // Samples: Revenant Horror I; Tarantula Broodfather IV

        for (line in ScoreboardData.sidebarLinesFormatted) {
            val noColorLine = line.removeColor()
            val match = slayerRegex.matcher(noColorLine)
            if (match.matches()) {
                slayerName = match.group("name")
                slayerLevel = match.group("level")
            } else if (noColorLine == "Slay the boss!") bossAlive = "slaying"
            else if (noColorLine == "Boss slain!") bossAlive = "slain"
        }

        if (slayerLevel == "") "Planning to do a slayer quest"// selected slayer in rpc but hasn't started a quest
        else if (bossAlive == "spawning") "Spawning a $slayerName $slayerLevel boss."
        else if (bossAlive == "slaying") "Slaying a $slayerName $slayerLevel boss."
        else if (bossAlive == "slain") "Finished slaying a $slayerName $slayerLevel boss."
        else "Something went wrong with slayer detection!"
    }),

    CUSTOM_STATE({
        be.hize.nes.NES.feature.misc.discordRPC.customFirstLine.get()
    }),

    CUSTOM_DETAIL({
        be.hize.nes.NES.feature.misc.discordRPC.customSecondLine.get()
    }),
    CROP_MILESTONES({
        val crop = InventoryUtils.getItemInHand()?.getCropType()
        val cropCounter = crop?.getCounter()
        val tier = cropCounter?.let { GardenCropMilestones.getTierForCrops(it) }

        val progress = tier?.let {
            LorenzUtils.formatPercentage(crop.progressToNextLevel())
        } ?: 100

        if (tier != null) {
            "${crop.cropName} $tier ($progress)"
        } else {
            "Not farming!"
        }
    }),
    AUTO({
        var r = ""
        val crops = CROP_MILESTONES.displayMessageSupplier!!.get()
        val coins = PURSE.displayMessageSupplier!!.get()
        val islandType = HypixelData.skyBlockIsland
        r = if (crops.isNotEmpty() && islandType == IslandType.GARDEN) crops
        else if (islandType == IslandType.THE_RIFT) MOTES_GOAL.displayMessageSupplier!!.get()
        else coins
        r
    }),
    MOTES_GOAL({
        val islandType = HypixelData.skyBlockIsland
        if (islandType != IslandType.THE_RIFT) {
            "Not In Rift!"
        } else {
            val s = PURSE.displayMessageSupplier!!.get().replace(" Motes", "")
            if (s.contains(" §5")) {
                "${s.split(" §5")[0]}+/14M Motes"
            } else {
                "$s/14M Motes"
            }
        }
    })
    ;

    fun getDisplayString(): String {
        if (displayMessageSupplier != null) {
            return displayMessageSupplier.get()
        }
        return ""
    }
}
