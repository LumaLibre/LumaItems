package dev.lumas.lumaitems.items.weapons.bow

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItem
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.extensions.syncTimer
import java.util.function.Consumer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataType

class PumpkinLauncherItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#fb6c1b&lP&#fb751b&lu&#fb7f1a&lm&#fb881a&lp&#fb921a&lk&#fb9b1a&li&#fba519&ln &#fbae19&lL&#fba519&la&#fb9b1a&lu&#fb921a&ln&#fb881a&lc&#fb7f1a&lh&#fb751b&le&#fb6c1b&lr",
            mutableListOf("&#fb9638J&#fb9a33a&#fb9e2ec&#fba229k&#fba623-&#fbaa1eO&#fbae19'"),
            mutableListOf("&#fb7a43\"&#fb7d42A&#fb8041b&#fb8440s&#fb873eo&#fb8a3dl&#fb8d3cu&#fb913bt&#fb943ae&#fb9739l&#fb9a38y &#fb9e37m&#fba135a&#fba434n&#fba733i&#fbaa32a&#fbae31c&#fbb130a&#fbb42fl&#fbb72d.&#fbbb2c.&#fbbe2b.&#fbc12a\"","","Upon charging and releasing, this","bow will launch a pumpkin that","explodes on impact"),
            Material.BOW,
            mutableListOf("pumpkinlauncher"),
            mutableMapOf(Enchantment.POWER to 7, Enchantment.INFINITY to 1, Enchantment.UNBREAKING to 10, Enchantment.MENDING to 1)
        )
        item.tier = "&#c46bfb&lH&#c86eee&la&#cd71e2&ll&#d174d5&ll&#d677c8&lo&#da7abc&lm&#de7daf&la&#e380a2&lr&#e78395&le&#eb8689&ls &#f0897c&l2&#f48c6f&l0&#f98f63&l2&#fd9256&l3"
        return Pair("pumpkinlauncher", item.createItem())
    }


    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.PROJECTILE_LAUNCH -> {
                event as ProjectileLaunchEvent
                replaceProjectile(event.entity, event)
            }
            Action.PROJECTILE_LAND -> {
                event as ProjectileHitEvent
                jackOLand(event.entity, player)
            }
            else -> return false
        }
        return true
    }

    private fun replaceProjectile(projectile: Projectile, event: ProjectileLaunchEvent) {
        val player = projectile.shooter as? Player ?: return
        if (player.hasMetadata("pumpkinLauncher")) {
            event.isCancelled = true
            return
        }
        player.setMetadata("pumpkinLauncher", FixedMetadataValue(instance(), true))

        val armorStand: ArmorStand = projectile.world.spawnEntity(projectile.location, EntityType.ARMOR_STAND) as ArmorStand
        armorStand.equipment.helmet = ItemStack(Material.JACK_O_LANTERN);
        armorStand.isInvisible = true
        armorStand.isInvulnerable = false
        armorStand.setGravity(false);
        armorStand.persistentDataContainer.set(NamespacedKey(instance(), "pumpkinlauncher"), PersistentDataType.SHORT, 1)

        armorStand.syncTimer(0, 1) { task ->
            val loc: Location = projectile.location.add(0.0,-2.0,0.0);
            loc.setDirection(player.location.toVector().subtract(armorStand.location.toVector()).normalize());

            armorStand.teleport(loc);
            projectile.world.spawnParticle(Particle.LAVA, projectile.location, 3, 0.1, 0.1, 0.1, 0.1);
            projectile.world.playSound(projectile.location, Sound.ENTITY_WITCH_CELEBRATE, 1f, 1f);
            if (projectile.isDead){
                task.cancel();
            }
        }
        for (entity in projectile.getNearbyEntities(100.0,100.0,100.0)) {
            if (entity is Player) {
                entity.hideEntity(instance(), projectile)
            }
        }


        projectile.setGravity(false);
        projectile.persistentDataContainer.set(NamespacedKey(instance(), "pumpkinlauncher"), PersistentDataType.SHORT, 1)

        projectile.syncDelayed(100) {
            if (!projectile.isDead) {
                jackOLand(projectile, player);
            }
        }
    }

    private fun jackOLand(projectile: Projectile, player: Player) {
        projectile.getNearbyEntities(4.0, 4.0, 4.0).forEach(Consumer { entity: Entity ->
            if (entity.persistentDataContainer.has(NamespacedKey(instance(), "pumpkinlauncher"), PersistentDataType.SHORT)) {
                entity.remove()
            }
        })

        projectile.world.createExplosion(projectile.location, 2f, false, false, player)
        projectile.remove()
        player.removeMetadata("pumpkinLauncher", instance())
    }


}