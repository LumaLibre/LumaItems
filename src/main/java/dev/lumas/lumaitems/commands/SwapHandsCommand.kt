package dev.lumas.lumaitems.commands

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.wrappers.EnumWrappers
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierCommand
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.hooks.ProtocolLibHook
import dev.lumas.lumaitems.registry.Registry
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "swaphands",
    aliases = ["offhand", "ofh"],
    description = "Command which sends a packet to swap the player's hands",
    usage = "/<command>",
    playerOnly = true
)
class SwapHandsCommand : BrigadierCommand() {

    @BrigadierExecutor
    fun run(src: CommandSourceStack) {
        val player = src.sender as Player

        val protocolManager = Registry.HOOKS.getOrThrow(ProtocolLibHook::class).getProtocolManager() ?: run {
            Text.msg(player, "Couldn't find ProtocolLib!")
            return
        }

        val packet = protocolManager.createPacket(PacketType.Play.Client.BLOCK_DIG)
        packet.playerDigTypes.write(0, EnumWrappers.PlayerDigType.SWAP_HELD_ITEMS)
        protocolManager.receiveClientPacket(player, packet)
        Text.msg(player, "Swapped hands!")
    }
}