package dev.lumas.lumaitems.util;

import dev.lumas.lumaitems.enums.Action;
import dev.lumas.lumaitems.enums.GenericToolType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ServiceDeterrents {

    private static final int DURATION = 80;

    private static final PotionEffect MINING_FATIGUE = new PotionEffect(PotionEffectType.MINING_FATIGUE, DURATION, 5);
    private static final PotionEffect WEAKNESS = new PotionEffect(PotionEffectType.WEAKNESS, DURATION, 5);
    private static final PotionEffect SLOWNESS = new PotionEffect(PotionEffectType.SLOWNESS, DURATION, 2);


    public static boolean applyDeterrent(@Nullable ItemStack item, @Nullable Player player, @NotNull Object event, @NotNull Action action) {
        if (item == null || player == null) return false;
        for (Deterrent deterrent : Deterrent.values()) {
            if (deterrent.apply(item, player, event, action)) {
                return true;
            }
        }
        return false;
    }

    public enum Deterrent {
        TOOL_FATIGUE(GenericToolType.TOOL, (item, player, event) -> {
            player.addPotionEffect(MINING_FATIGUE);
            if (event instanceof BlockDamageEvent handle) {
                handle.setCancelled(true);
            }
        },
                Action.BREAK_BLOCK, Action.CACHED_BLOCK_BREAK, Action.BLOCK_DAMAGE, Action.FISH, Action.LEFT_CLICK, Action.RIGHT_CLICK),
        WEAPON_WEAKNESS(GenericToolType.WEAPON, (item, player, event) -> {
            player.addPotionEffect(WEAKNESS);
            if (event instanceof EntityDamageByEntityEvent damageEvent) {
                damageEvent.setCancelled(true);
            }
        },
                Action.LEFT_CLICK, Action.RIGHT_CLICK, Action.ENTITY_DAMAGE, Action.ENTITY_DAMAGED_BY_PLAYER, Action.ENTITY_DEATH),
        ARMOR(GenericToolType.ARMOR, (item, player, event) -> {
            player.addPotionEffect(SLOWNESS);
            if (event instanceof EntityDamageEvent damageEvent) {
                if (damageEvent.isApplicable(EntityDamageEvent.DamageModifier.ARMOR)) {
                    damageEvent.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0.0);
                }
                if (damageEvent.isApplicable(EntityDamageEvent.DamageModifier.MAGIC)) {
                    damageEvent.setDamage(EntityDamageEvent.DamageModifier.MAGIC, 0.0);
                }
                if (damageEvent.isApplicable(EntityDamageEvent.DamageModifier.HARD_HAT)) {
                    damageEvent.setDamage(EntityDamageEvent.DamageModifier.HARD_HAT, 0.0);
                }
                if (damageEvent.isApplicable(EntityDamageEvent.DamageModifier.BLOCKING)) {
                    damageEvent.setDamage(EntityDamageEvent.DamageModifier.BLOCKING, 0.0);
                }
            } else if (event instanceof EntityBlockFormEvent handle) {
                handle.setCancelled(true);
            }
        }, Action.PLAYER_DAMAGED, Action.PLAYER_DAMAGED_BY_ENTITY, Action.ENTITY_FORM_BLOCK);

        private final GenericToolType genericToolType;
        private final TriConsumer<ItemStack, Player, Event> consumer;
        private final List<Action> actions;

        Deterrent(GenericToolType genericToolType, TriConsumer<ItemStack, Player, Event> consumer, Action... actions) {
            this.genericToolType = genericToolType;
            this.consumer = consumer;
            this.actions = List.of(actions);
        }

        public boolean apply(ItemStack item, Player player, Object event, Action action) {
            if (genericToolType == GenericToolType.getGenericToolType(item.getType()) && actions.contains(action)) {
                consumer.accept(item, player, (Event) event);
                return true;
            }
            return false;
        }
    }
}
