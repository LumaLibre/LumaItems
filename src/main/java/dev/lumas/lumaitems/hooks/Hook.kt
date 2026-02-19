package dev.lumas.lumaitems.hooks

import dev.lumas.lumaitems.registry.RegistryItem
import kotlin.reflect.KClass
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

interface Hook : RegistryItem {

    fun isWith(): Boolean = instance() != null

    fun instance(): Plugin? = Bukkit.getPluginManager().getPlugin(this.identifier().asSimpleString())

    fun <T : Plugin> typedInstance(clazz: Class<T>): T? {
        val plugin = instance() ?: return null
        @Suppress("UNCHECKED_CAST")
        return plugin as? T
    }

    fun <T : Plugin> typedInstance(clazz: KClass<T>): T? {
        return typedInstance(clazz.java)
    }
}