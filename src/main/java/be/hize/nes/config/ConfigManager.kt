package be.hize.nes.config

import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUItems
import be.hize.nes.NES
import be.hize.nes.utils.NESLogger
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.moulberry.moulconfig.observer.PropertyTypeAdapterFactory
import io.github.moulberry.moulconfig.processor.BuiltinMoulConfigGuis
import io.github.moulberry.moulconfig.processor.ConfigProcessorDriver
import io.github.moulberry.moulconfig.processor.MoulConfigProcessor
import net.minecraft.item.ItemStack
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.concurrent.fixedRateTimer

class ConfigManager {
    companion object {
        val gson = GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeSpecialFloatingPointValues()
            .registerTypeAdapterFactory(PropertyTypeAdapterFactory())
            .registerTypeAdapter(UUID::class.java, object : TypeAdapter<UUID>() {
                override fun write(out: JsonWriter, value: UUID) {
                    out.value(value.toString())
                }

                override fun read(reader: JsonReader): UUID {
                    return UUID.fromString(reader.nextString())
                }
            }.nullSafe())
            .registerTypeAdapter(LorenzVec::class.java, object : TypeAdapter<LorenzVec>() {
                override fun write(out: JsonWriter, value: LorenzVec) {
                    value.run { out.value("$x:$y:$z") }
                }

                override fun read(reader: JsonReader): LorenzVec {
                    val (x, y, z) = reader.nextString().split(":").map { it.toDouble() }
                    return LorenzVec(x, y, z)
                }
            }.nullSafe())
            .registerTypeAdapter(ItemStack::class.java, object : TypeAdapter<ItemStack>() {
                override fun write(out: JsonWriter, value: ItemStack) {
                    out.value(NEUItems.saveNBTData(value))
                }

                override fun read(reader: JsonReader): ItemStack {
                    return NEUItems.loadNBTData(reader.nextString())
                }
            }.nullSafe())
            .enableComplexMapKeySerialization()
            .create()
    }

    lateinit var features: Features
        private set
    private val logger = NESLogger("config_manager")

    var configDirectory = File("config/nes")
    private var configFile: File? = null
    lateinit var processor: MoulConfigProcessor<Features>

    fun firstLoad() {
        if (::features.isInitialized) {
            logger.log("Loading config despite config being already loaded?")
        }
        configDirectory.mkdir()

        configFile = File(configDirectory, "config.json")

        fixedRateTimer(name = "nes-config-auto-save", period = 60_000L, initialDelay = 60_000L) {
            saveConfig("auto-save-60s")
        }

        logger.log("Trying to load config from $configFile")

        if (configFile!!.exists()) {
            try {
                val inputStreamReader = InputStreamReader(FileInputStream(configFile!!), StandardCharsets.UTF_8)
                val bufferedReader = BufferedReader(inputStreamReader)
                val builder = StringBuilder()
                for (line in bufferedReader.lines()) {
                    builder.append(line)
                    builder.append("\n")
                }


                logger.log("load-config-now")
                features = gson.fromJson(
                    builder.toString(),
                    Features::class.java
                )
                logger.log("Loaded config from file")
            } catch (error: Exception) {
                error.printStackTrace()
                val backupFile = configFile!!.resolveSibling("config-${System.currentTimeMillis()}-backup.json")
                logger.log("Exception while reading $configFile. Will load blank config and save backup to $backupFile")
                logger.log("Exception was $error")
                try {
                    configFile!!.copyTo(backupFile)
                } catch (e: Exception) {
                    logger.log("Could not create backup for config file")
                    e.printStackTrace()
                }
            }
        }

        if (!::features.isInitialized) {
            logger.log("Creating blank config and saving to file")
            features = Features()
            saveConfig("blank config")
        }

        val features = NES.feature
        processor = MoulConfigProcessor(NES.feature)
        BuiltinMoulConfigGuis.addProcessors(processor)
        val configProcessorDriver = ConfigProcessorDriver(processor)
        configProcessorDriver.processConfig(features)
    }

    fun saveConfig(reason: String) {
        logger.log("saveConfig: $reason")
        val file = configFile ?: throw Error("Can not save config, configFile is null!")
        try {
            logger.log("Saving config file")
            file.parentFile.mkdirs()
            file.createNewFile()
            BufferedWriter(OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8)).use { writer ->
                // TODO remove old "hidden" area
                writer.write(gson.toJson(NES.feature))
            }
        } catch (e: IOException) {
            logger.log("Could not save config file to $file")
            e.printStackTrace()
        }
    }
}
