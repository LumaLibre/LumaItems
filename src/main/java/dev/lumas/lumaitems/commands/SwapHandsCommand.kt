package dev.lumas.lumaitems.commands

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.wrappers.EnumWrappers
import dev.lumas.lumacore.manager.commands.AbstractCommand
import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.util.MiniMessageUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.COMMAND)
@CommandInfo(
    name = "swaphands",
    aliases = ["offhand", "ofh"],
    description = "Command which sends a packet to swap the player's hands",
    usage = "/<command>",
    playerOnly = true
)
class SwapHandsCommand : AbstractCommand() {

    override fun handle(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val protocolManager = LumaItems.getProtocolManager() ?: run {
            MiniMessageUtil.msg(sender, "Couldn't find ProtocolLib!")
            return true
        }
        sender as? Player ?: run {
            MiniMessageUtil.msg(sender, "Only players can use this command!")
            return true
        }

        val packet = protocolManager.createPacket(PacketType.Play.Client.BLOCK_DIG)
        packet.playerDigTypes.write(0, EnumWrappers.PlayerDigType.SWAP_HELD_ITEMS)
        protocolManager.receiveClientPacket(sender, packet)
        MiniMessageUtil.msg(sender, "Swapped hands!")
        return true
    }

    override fun handleTabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String>? {
        return null
    }

}