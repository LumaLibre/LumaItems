package dev.jsinco.luma.lumaitems.commands

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.wrappers.EnumWrappers
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SwapHandsCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
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
}