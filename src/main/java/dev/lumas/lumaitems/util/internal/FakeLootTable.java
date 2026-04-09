package dev.lumas.lumaitems.util.internal;

import com.google.common.base.Preconditions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NullMarked
public final class FakeLootTable {

    private FakeLootTable() {}

    public static List<org.bukkit.inventory.ItemStack> adapterSimulate(World world, org.bukkit.entity.LivingEntity entity, org.bukkit.entity.@Nullable Player player, org.bukkit.damage.DamageSource damageSource, int lootingLevel) {
        ServerLevel serverLevel = ((CraftWorld) world).getHandle();
        LivingEntity delegateEntity = ((CraftLivingEntity) entity).getHandle();
        Player delegatePlayer = player != null ? ((CraftPlayer) player).getHandle() : null;
        DamageSource delegateDamageSource = ((CraftDamageSource) damageSource).getHandle();

        List<ItemStack> drops = simulateDrops(serverLevel, delegateEntity, delegatePlayer, delegateDamageSource, lootingLevel);
        return drops.stream().map(CraftItemStack::asBukkitCopy).toList();
    }

    public static List<ItemStack> simulateDrops(ServerLevel level, LivingEntity entity, @Nullable Player killer, DamageSource damageSource, int lootingLevel) {
        Optional<ResourceKey<LootTable>> lootTableKey = entity.getLootTable();
        if (lootTableKey.isEmpty()) return List.of();

        LootTable lootTable = level.getServer()
                .reloadableRegistries()
                .getLootTable(lootTableKey.get());

        LivingEntity fakeAttacker = createFakeAttacker(level, lootingLevel);

        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, fakeAttacker)
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, fakeAttacker);

        if (killer != null) {
            builder = builder
                    .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, killer)
                    .withLuck(killer.getLuck());
        }

        LootParams lootParams = builder.create(LootContextParamSets.ENTITY);

        List<ItemStack> drops = new ArrayList<>();
        lootTable.getRandomItems(lootParams, entity.getLootTableSeed(), drops::add);
        return drops;
    }

    private static LivingEntity createFakeAttacker(ServerLevel level, int lootingLevel) {
        net.minecraft.world.entity.decoration.ArmorStand armorStand =
                new net.minecraft.world.entity.decoration.ArmorStand(EntityType.ARMOR_STAND, level);

        if (lootingLevel > 0) {
            ItemStack fakeWeapon = new ItemStack(Items.STICK);

            Holder<Enchantment> looting = level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.LOOTING);

            fakeWeapon.enchant(looting, lootingLevel);
            armorStand.setItemSlot(EquipmentSlot.MAINHAND, fakeWeapon);
        }

        return armorStand;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private @Nullable World world;
        private org.bukkit.entity.@Nullable LivingEntity entity;
        private org.bukkit.entity.@Nullable Player player;
        private org.bukkit.damage.@Nullable DamageSource damageSource;
        private int lootingLevel;

        public Builder world(World world) {
            this.world = world;
            return this;
        }

        public Builder entity(org.bukkit.entity.LivingEntity entity) {
            this.entity = entity;
            return this;
        }

        public Builder player(org.bukkit.entity.@Nullable Player player) {
            this.player = player;
            return this;
        }

        public Builder damageSource(org.bukkit.damage.DamageSource damageSource) {
            this.damageSource = damageSource;
            return this;
        }

        public Builder looting(int lootingLevel) {
            this.lootingLevel = lootingLevel;
            return this;
        }

        public List<org.bukkit.inventory.ItemStack> simulate() {
            Preconditions.checkNotNull(world, "World must be set");
            Preconditions.checkNotNull(entity, "Entity must be set");
            Preconditions.checkNotNull(damageSource, "DamageSource must be set");

            return adapterSimulate(world, entity, player, damageSource, lootingLevel);
        }
    }
}