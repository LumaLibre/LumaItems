package dev.jsinco.luma.lumaitems.items.tools.mattock

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.PersistentDataRecord
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.dialogue.DialogueText
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import java.util.UUID
import kotlin.random.Random
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class SplitSoulMultiToolBagItem : CustomItemFunctions() {

    private val parent = SplitSoulMultiToolItem()

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#FB0843>S<#E6367E>p<#D063BA>l<#BB91F5>i<#C5A5DE>t<#CFB9C7>S<#DACDAF>o<#E4E198>u<#EEF581>l <#80C5C0>B<#48ACDF>a<#1194FE>g")
            .customEnchants(
                "<#555555>Multi-Tool",
                "<gold>Personality"
            )
            .material(Material.GRAY_BUNDLE)
            .persistentData("splitsoul-multi-tool-bag")
            .tier(Tier.EASTER_2025)
            .vanillaEnchants(
                Enchantment.UNBREAKING to 10
            )
            .lore(
                "<dark_gray>Right-click to redeem!",
                "",
                "Beware of the various personalities",
                "that this item may have...",
                "",
                "This item can swap between all tool",
                "types. Left-click any block to swap",
                "to the preferred tool type.",
                "",
                "Press your <gray>swap key (F)</gray> to switch",
                "between silk touch and fortune."
            )
            .buildPair()
    }


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val item = event.item ?: return
        item.amount -= 1

        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1f)
        Util.giveItem(player, parent.create().createItem())
    }
}

class SplitSoulMultiToolItem : CustomItemFunctions() {

    companion object {
        private const val KEY = "splitsoul-multi-tool"
        private const val MOODY_ACTION_BOUND = 25000
        private const val MOODY_DIALOGUE_BOUND = 9000
        private val TRANSITIONABLE_MATERIALS = listOf(
            ItemStack.of(Material.NETHERITE_PICKAXE),
            ItemStack.of(Material.NETHERITE_SHOVEL),
            ItemStack.of(Material.NETHERITE_AXE),
            ItemStack.of(Material.NETHERITE_HOE),
            ItemStack.of(Material.SHEARS),
            ItemStack.of(Material.NETHERITE_SWORD)
        )
        private fun trMatsExcluding(excluding: Material) = TRANSITIONABLE_MATERIALS.filter { it.type != excluding }
        private val activeMoods: MutableMap<UUID, HeldMoodyAction> = mutableMapOf()
    }


    fun create(): ItemFactory {
        val personality = Personality.entries.random()
        val color = personality.hexColor
        return ItemFactory.builder()
            .name("<b><#FB0843>S<#EB2A70>p<#DB4D9C>l<#CB6FC9>i<#BB91F5>t<#C19CE8>S<#C6A7DB>o<#CCB2CE>u<#D2BDC1>l <#DDD4A8>M<#E3DF9B>u<#E8EA8E>l<#EEF581>t<#C9E596>i<#A4D5AB>-<#80C5C0>T<#5BB4D4>o<#36A4E9>o<#1194FE>l")
            .customEnchants(
                "<$color>Personality",
                "<dark_gray>Multi-Tool"
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 8,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.UNBREAKING to 10,
                Enchantment.SHARPNESS to 8,
                Enchantment.LOOTING to 4,
                Enchantment.MENDING to 1,
            )
            .quotes(
                "<$color>\"Don't listen to the voices...\""
            )
            .lore(
                "This item can swap between all tool",
                "types. Left-click any block to swap",
                "to the preferred tool type.",
                "",
                "Press your <$color>swap key (F)</$color> to switch",
                "between silk touch and fortune."
            )
            .persistentDataRecords(
                PersistentDataRecord.create("personality", PersistentDataType.STRING, personality.name),
            )
            .tier(Tier.EASTER_2025)
            .persistentData(KEY)
            .material(Material.NETHERITE_PICKAXE)
            .build()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return Pair(KEY, create().createItem())
    }

    private fun preChecks(player: Player, item: ItemStack, event: Cancellable): Boolean {
        if (!Util.isItemInSlot(KEY, EquipmentSlot.HAND, player)) return false

        if (activeMoods.contains(player.uniqueId)) {
            doHeldMoodyAction(item, event, player)
        } else if (random().nextInt(0, MOODY_ACTION_BOUND) == 1) {
            newMoodyAction(item, event, player)
        } else if (random().nextInt(0, MOODY_DIALOGUE_BOUND) == 1) {
            val personality = getPersonality(item)
            val dialogue = MoodyDialogue.entries.random()
            dialogue.play(player, personality)
        }
        return true
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        val item = player.inventory.itemInMainHand
        val (remove, add, level) = if (item.containsEnchantment(Enchantment.SILK_TOUCH)) {
            Triple(Enchantment.SILK_TOUCH, Enchantment.FORTUNE, 4)
        } else {
            Triple(Enchantment.FORTUNE, Enchantment.SILK_TOUCH, 1)
        }
        item.removeEnchantment(remove)
        item.addUnsafeEnchantment(add, level)
        event.isCancelled = true
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        val item = player.inventory.itemInMainHand
        if (!preChecks(player, item, event)) return
        @Suppress("DEPRECATION")
        item.type = Material.NETHERITE_SWORD
    }

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        val item = player.inventory.itemInMainHand
        if (!preChecks(player, item, event)) return

        var highestDestroySpeed: Pair<Material, Float> = Pair(item.type, block.getDestroySpeed(item))
        for (itemStack in trMatsExcluding(item.type)) {
            val destroySpeed = block.getDestroySpeed(itemStack)
            if (destroySpeed <= highestDestroySpeed.second) {
                continue
            }
            highestDestroySpeed = Pair(itemStack.type, destroySpeed)
        }

        if (highestDestroySpeed.first != item.type) {
            @Suppress("DEPRECATION")
            item.type = highestDestroySpeed.first
        }
    }

    override fun onPlayerItemDamage(player: Player, event: PlayerItemDamageEvent) {
        val item = event.item
        if (item.type != Material.SHEARS) return

        // Only damage shears 11.71% of the time to match the health of Netherite gear
        val chanceToDamage = 238.0 / 2031.0

        if (Math.random() > chanceToDamage) {
            event.damage = 0
        }
    }

    /**
     * Attempts to perform a new moody action.
     *
     * @param itemStack The item stack being used.
     * @param event The event that triggered the action.
     * @param player The player using the item.
     *
     * @return True if the calling function should return and not transform the item.
     */
    private fun newMoodyAction(itemStack: ItemStack, event: Cancellable, player: Player) {
        val personality = getPersonality(itemStack)
        val action = MoodyAction.entries.random()
        if (action.cancelEvent) {
            event.isCancelled = true
        }
        action.perform(player, personality)
    }

    /**
     * Performs a held moody action. Repeats itself every time the player
     * tries to perform an action with the item until it expires.
     *
     * @param item The item stack being used.
     * @param event The event that triggered the action.
     * @param player The player using the item.
     *
     * @return True if the calling function should return and not transform the item.
     */
    private fun doHeldMoodyAction(item: ItemStack, event: Cancellable, player: Player) {
        val heldMoodyAction = activeMoods[player.uniqueId] ?: return
        if (heldMoodyAction.isExpired()) {
            activeMoods.remove(player.uniqueId)
            return
        }
        val personality = getPersonality(item)
        val dialogue = heldMoodyAction.dialogue.apply {
            if (ifAbsentColor == personality.textColor) return@apply
            ifAbsentColor = personality.textColor
            voicePitch = personality.voicePitch
            textSpeed = personality.dialogueSpeed
        }

        val action = heldMoodyAction.action
        if (action.cancelEvent) {
            event.isCancelled = true
        }

        action.performHold(player, personality, dialogue)
    }

    private fun getPersonality(itemStack: ItemStack): Personality {
        val personalityName = Util.getPersistentKey(itemStack.itemMeta, "personality", PersistentDataType.STRING)
            ?: return Personality.PLEASANT
        return Personality.valueOf(personalityName)
    }



    enum class Personality(val hexColor: String, val voicePitch: Float, val dialogueSpeed: Long) {
        PLEASANT("#BB91F5", 6.0f, 60),
        LAZY("#EEF581", 3.0f, 50),
        SAD("#1194FE", 0.75f, 55),
        UPSET("#FB0843", 0.3f, 60),
        ;

        val textColor: TextColor = TextColor.fromHexString(hexColor) ?: NamedTextColor.DARK_GRAY
    }

    enum class MoodyDialogue(
        vararg messages: Pair<Personality, String>
    ) {

        A(
            Personality.PLEASANT to "Spending time with you is so much fun!#I love it!",
            Personality.UPSET to "I hate you.#I hate you.#I hate you.#I hate you.#I hate you.",
            Personality.SAD to "I miss them.#I miss everything.#Everything feels so empty now.#I don't know what to do.#I just want to sleep.",
            Personality.LAZY to "I can't do it today.#Everything feels too heavy.#Maybe later.#I just want to lie here."
        ),
        B(
            Personality.PLEASANT to "The colors are so beautiful today!#I love the way the light shines through the trees.",
            Personality.UPSET to "This day is so awful.#It's almost as unbearable as you.",
            Personality.SAD to "I don't want to be here.#I don't want to be anywhere.#I just want to be left alone.",
            Personality.LAZY to "I can't even get up.#Why bother?#Maybe later.#It's just too much effort."
        ),
        C(
            Personality.PLEASANT to "It's such a beautiful day!#I feel like I could do anything right now!",
            Personality.UPSET to "This is the worst.#I don't understand why everything goes wrong for me.",
            Personality.SAD to "I just want to disappear.#I don't know how to keep going.#Everything feels so pointless.",
            Personality.LAZY to "Ugh, what's the point?#I don't even feel like moving.#I’ll just stay here for now."
        )
        ;

        private val messages = mutableMapOf<Personality, MutableList<String>>().also {
            for (message in messages) {
                it.computeIfAbsent(message.first) { mutableListOf() }.add(message.second)
            }
        }

        fun play(player: Player, personality: Personality) {
            val dialogue = DialogueText(
                player = player,
                ifAbsentColor = personality.textColor,
                voicePitch = personality.voicePitch,
                textSpeed = personality.dialogueSpeed
            )
            val messages = this.messages[personality]?.random()?.split("#") ?: return
            dialogue.queueText(messages)
            dialogue.sendQueuedText(null)
        }
    }

    enum class MoodyAction(
        private val result: MoodyActionResult,
        val cancelEvent: Boolean = false,
        val block: (player: Player, personality: Personality) -> Unit,
        vararg messages: Pair<Personality, String>
    ) {

        REFUSE_TO_WORK(
            MoodyActionResult.HOLD_ACTION,
            cancelEvent = true,
            { player, _ ->
                MiniMessageUtil.msg(player, "Your tool doesn't appear to want to work right now. Try giving it some space...")
            },
            Personality.PLEASANT to "I-#I'm so sory, but I'm too exhausted to work...#I just need a little break...",
            Personality.LAZY to "Ugh, I don't want to work.",
            Personality.LAZY to "Seriously? You want me to work?#No thanks, not going to happen right now.",
            Personality.SAD to "Why do I have to be a tool?!#WHY ME!",
            Personality.UPSET to "I don't want to work, leave me alone.",
        ),
        BAD_EFFECTS_ON_PLAYER(
            { player, personality ->
                when (personality) {
                    Personality.UPSET -> listOf(PotionEffect(PotionEffectType.DARKNESS, 1200, 10))
                    Personality.SAD -> listOf(
                        PotionEffect(PotionEffectType.NAUSEA, 1200, 10),
                        PotionEffect(PotionEffectType.SLOWNESS, 1200, 2)
                    )
                    Personality.LAZY -> listOf(PotionEffect(PotionEffectType.SLOWNESS, 1200, 20))
                    Personality.PLEASANT -> listOf(
                        PotionEffect(PotionEffectType.HASTE, 1200, 2),
                        PotionEffect(PotionEffectType.SPEED, 1200, 2)
                    )
                }.forEach {
                    player.addPotionEffect(it)
                }
            },
            Personality.PLEASANT to "You must be tired!#Here,#let me help you out a bit!",
            Personality.LAZY to "Come on,#I don't want to work anymore.#I just want to relax.#Let's take a break-",
            Personality.SAD to "Please stop!#I feel sick.#I can't take this anymore!",
            Personality.UPSET to "You deserve to see nothing...",
        ),
        CHORUS_FRUIT_PLAYER(
            { player, personality ->
                if (personality != Personality.PLEASANT && personality != Personality.SAD) {
                    val range = if (personality == Personality.UPSET) 100 else 30
                    player.teleportAsync(player.location.add(
                        Random.nextInt(1, range).toDouble(),
                        Random.nextInt(1, range).toDouble(),
                        Random.nextInt(1, range).toDouble()
                    ))
                }
            },
            Personality.PLEASANT to "*cough*#*cough*",
            Personality.LAZY to "Let's take a stroll!",
            Personality.SAD to "*sobbing*",
            Personality.UPSET to "*laughing*",
            Personality.UPSET to "Enjoy your little interruption...",
        ),
        EXPLODE_BROKEN_BLOCK(
            block@{ player, personality ->
                val breakBlocks = !AbilityUtil.noBuildPermission(player, player.location.block)
                val power = when (personality) {
                    Personality.PLEASANT -> return@block
                    Personality.LAZY, Personality.SAD -> 4.0f
                    Personality.UPSET -> 10.0f
                }
                player.world.createExplosion(player.location, power, breakBlocks, breakBlocks)
            },
            Personality.PLEASANT to "Ah, isn't it a beautiful day?",
            Personality.LAZY to "Here, let me help you out a bit.",
            Personality.SAD to "Please, stop hitting me against things...#I can't take this anymore!",
            Personality.UPSET to "Hahaha#AHAAHAHAHAAHAHAAHAHAA",
        ),
        ;

        constructor(block: (player: Player, personality: Personality) -> Unit, vararg messages: Pair<Personality, String>) : this(
            MoodyActionResult.ONE_TIME_ACTION,
            false,
            block,
            *messages
        )

        private val messages = mutableMapOf<Personality, MutableList<String>>().also {
            for (message in messages) {
                it.computeIfAbsent(message.first) { mutableListOf() }.add(message.second)
            }
        }

        fun performHold(player: Player, personality: Personality, dialogue: DialogueText) {
            val messages = this.messages[personality]?.random()?.split("#") ?: return
            dialogue.queueText(messages)
            dialogue.sendQueuedText {
                Bukkit.getScheduler().runTask(LumaItems.getInstance(), Runnable {
                    this.block.invoke(player, personality)
                })
            }
        }

        fun perform(player: Player, personality: Personality) {
            val messages = this.messages[personality]?.random()?.split("#") ?: return
            val dialogue = DialogueText(
                player = player,
                ifAbsentColor = personality.textColor,
                voicePitch = personality.voicePitch,
                textSpeed = personality.dialogueSpeed
            )
            dialogue.queueText(messages)
            dialogue.sendQueuedText {
                Bukkit.getScheduler().runTask(LumaItems.getInstance(), Runnable {
                    this.block.invoke(player, personality)
                })
            }

            if (this.result == MoodyActionResult.HOLD_ACTION) {
                val heldMoodyAction = HeldMoodyAction(
                    expireTime = System.currentTimeMillis() + 5 * 60 * 1000L,
                    action = this,
                    dialogue = dialogue,
                )
                activeMoods[player.uniqueId] = heldMoodyAction
            }
        }
    }

    enum class MoodyActionResult {
        HOLD_ACTION, // Creates held moody action
        ONE_TIME_ACTION, // performs the action once
    }

    /**
     * A moody action that repeats itself whenever called for X amount of time until it expires.
     */
    class HeldMoodyAction(
        val expireTime: Long,
        val action: MoodyAction,
        val dialogue: DialogueText
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() >= expireTime
        }
    }
}