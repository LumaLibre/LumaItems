package dev.jsinco.luma.manager

import dev.jsinco.luma.LumaItems
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.nio.file.Files

class FileManager(val fileName: String) {

    companion object {
        private val plugin: LumaItems = LumaItems.getInstance()
        private val defaultFiles: List<String> = listOf("relics.yml", "config.yml")

        @JvmStatic fun generateFolder(folder: String) {
            if (!File(plugin.dataFolder, folder).exists()) {
                File(plugin.dataFolder, folder).mkdir()
            }
        }

        @JvmStatic fun generateDefaultFiles() {
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdir()
            }
            generateFolder("saves")
            for (fileName in defaultFiles) {
                FileManager(fileName).generateFile()
            }
        }
    }

    val file: File = File(plugin.dataFolder, fileName)
    private var yamlConfiguration: YamlConfiguration? = null


    fun generateYamlFile(): YamlConfiguration {
        generateFile()
        return getFileYaml()
    }

    fun generateFile() {
        try {
            if (!file.exists()) {
                file.createNewFile()

                val inputStream = plugin.getResource(fileName)
                if (inputStream != null) {
                    val outputStream = Files.newOutputStream(file.toPath())
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    inputStream.close()
                    outputStream.flush()
                    outputStream.close()
                }
            }
        } catch (ex: IOException) {
            plugin.logger.warning("Could not generate file: ${file.name} \n $ex")
        }
    }

    fun overrideFile() {
        try {
            if (!file.exists()) return

            val inputStream = plugin.getResource(fileName)
            if (inputStream != null) {
                val outputStream = Files.newOutputStream(file.toPath())
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                inputStream.close()
                outputStream.flush()
                outputStream.close()
            }
        } catch (ex: IOException) {
            plugin.logger.warning("Could not override file: ${file.name} \n $ex")
        }
    }

    fun deleteFile() {
        if (file.exists()) {
            file.delete()
        }
    }

    fun getFileYaml(): YamlConfiguration {
        yamlConfiguration = YamlConfiguration.loadConfiguration(file)
        return yamlConfiguration!!
    }

    fun saveFileYaml() {
        yamlConfiguration?.save(file)
    }

    fun reloadFileYaml() {
        yamlConfiguration?.load(file)
    }
}