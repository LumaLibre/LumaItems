package dev.lumas.lumaitems.hooks

import dev.lumas.lumaitems.registry.RegistryItem
import org.bukkit.Bukkit

interface Hook : RegistryItem {

    fun isWith(): Boolean = Bukkit.getPluginManager().getPlugin(this.identifier().asSimpleString()) != null
}