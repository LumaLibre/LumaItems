package dev.jsinco.luma.lumaitems.commands.subcommands;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import dev.jsinco.luma.lumacore.manager.commands.CommandInfo;
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister;
import dev.jsinco.luma.lumacore.manager.modules.RegisterType;
import dev.jsinco.luma.lumaitems.LumaItems;
import dev.jsinco.luma.lumaitems.commands.CommandManager;
import dev.jsinco.luma.lumaitems.commands.SubCommand;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
        name = "debug",
        description = "Debug command",
        usage = "/<command> debug",
        permission = "lumaitems.command.debug",
        playerOnly = false,
        parent = CommandManager.class
)
public class DebugCommand implements SubCommand {
    @Override
    public boolean execute(@NotNull LumaItems plugin, @NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        ProtocolManager protocolManager = LumaItems.getProtocolManager();
        //protocolManager.addPacketListener(new PacketListener(this,) {});
        return true;
    }

    @Nullable
    @Override
    public List<String> tabComplete(@NotNull LumaItems plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        return Arrays.stream(Material.values()).map(it -> it.name().toLowerCase()).toList();
    }

    private static class ChunkBiomePacketListener extends PacketAdapter {

        public ChunkBiomePacketListener(@NotNull AdapterParameteters params) {
            super(new AdapterParameteters().plugin(
                    LumaItems.getInstance()
            ).serverSide().types(
                    PacketType.Play.Server.CHUNKS_BIOMES
            ).listenerPriority(ListenerPriority.NORMAL));
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            PacketContainer packetContainer = event.getPacket();
            ClientboundChunksBiomesPacket clientboundChunksBiomesPacket = (ClientboundChunksBiomesPacket) packetContainer.getHandle();

            //clientboundChunksBiomesPacket.chunkBiomeData().get(0).
        }
    }
}
