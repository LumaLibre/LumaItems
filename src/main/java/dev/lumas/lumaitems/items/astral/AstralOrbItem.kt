package dev.lumas.lumaitems.items.astral

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.configuration.files.AstralYml
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.relics.RelicCrafting
import dev.lumas.lumaitems.util.Util
import kotlin.random.Random
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class AstralOrbItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        return Pair("astralorb", RelicCrafting.astralOrb)
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                event as PlayerInteractEvent
                event.isCancelled = true

                if (!player.inventory.itemInMainHand.itemMeta.persistentDataContainer.has(NamespacedKey(LumaItems.getInstance(), "astralorb"), PersistentDataType.SHORT)) return false

                player.inventory.itemInMainHand.amount -= 1
                player.playSound(player.location, Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 1f)
                for (globalPlayer in Bukkit.getOnlinePlayers()) {
                    globalPlayer.playSound(globalPlayer.location, Sound.ENTITY_EVOKER_CAST_SPELL, 0.9f, 1f)
                    globalPlayer.sendMessage(Util.colorcode("${Util.legacyPrefix} &#F7FFC9${player.name}&#E2E2E2 has revealed a relic inside of a &#AC87FB&lAstral &#F7FFC9Orb&#E2E2E2!"))
                }

                Util.giveItem(player, getAstralItem())
            }

            else -> return false
        }
        return true
    }
    private fun getAstralItem(): ItemStack {
        val setsWithWeights = Registry.CONFIGS.getOrThrow(AstralYml::class).astralOrbRarities

        var selectedSet = setsWithWeights.entries.random()
        while (selectedSet.value < Random.nextInt(101)) {
            selectedSet = setsWithWeights.entries.random()
        }

        val items = RelicCrafting.getItemsFromClass(selectedSet.key.getAstralSetClass())
        return items.random()
    }
}