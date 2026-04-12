package dev.lumas.lumaitems.items.misc.magical

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.spell.AbstractSpell
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.entity.FakeLightning
import dev.lumas.lumaitems.model.item.PersistentDataRecord
import dev.lumas.lumaitems.model.spell.SpellCaster
import dev.lumas.lumaitems.util.extensions.asEnum
import dev.lumas.lumaitems.util.extensions.formatEnumerator
import dev.lumas.lumaitems.util.extensions.getPersistentKey
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.send
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class UnnamedGemstoneItem : CustomItemFunctions() {

    companion object {
        private val key = "unnamed-gemstone".namespacedKey()
        private val spell_key = "unnamed-gemstone-spell".namespacedKey()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("unnamed gemstone")
            .material(Material.EMERALD)
            .persistentData("unnamed-gemstone")
            .buildPair()
    }

    private enum class Spell(override val cooldown: Long) : AbstractSpell {
        LOVE_BOMB(100L) {
            override fun onHit(player: Player, event: ProjectileHitEvent) {
                player.send("not yet implemented")
            }
        },
        LIGHTNING(200L) {
            override fun onHit(player: Player, event: ProjectileHitEvent) {
                val hitLocation = event.entity.location
                FakeLightning.builder()
                    .location(hitLocation)
                    .viewersFromRadius(100.0)
                    .build()
                    .strike()
            }
        }
        ;

        val persistentDataRecord = PersistentDataRecord.create(spell_key, PersistentDataType.STRING, name)

        abstract fun onHit(player: Player, event: ProjectileHitEvent)
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val item = event.item ?: return
        val currentSpell = item.getPersistentKey(spell_key, PersistentDataType.STRING)?.asEnum(Spell::class.java) ?: Spell.LIGHTNING

        if (player.isSneaking) {
            // get/set next spell
            val nextSpell = Spell.entries[(currentSpell.ordinal + 1) % Spell.entries.size]
            item.setPersistentKey(spell_key, PersistentDataType.STRING, nextSpell.name)
            player.send("Spell set to ${nextSpell.formatEnumerator()}")
            return
        }


        SpellCaster.builder()
            .player(player)
            .particle(Particle.FIREWORK)
            .key(key)
            .addSpellData(currentSpell.persistentDataRecord)
            .build()
            .cast()
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val spellName = event.entity.getPersistentKey(spell_key, PersistentDataType.STRING) ?: return
        val spell = spellName.asEnum(Spell::class.java) ?: return
        spell.onHit(player, event)
    }

}