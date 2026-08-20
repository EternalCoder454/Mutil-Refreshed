package se.mickelus.mutil.network;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Sends a packet to the server from the client.
 *
 * This was a method on PacketHandler behind an @OnlyIn(Dist.CLIENT). NeoForge does not strip
 * @OnlyIn from mod classes, so PacketHandler carried a reference to Minecraft in its own bytecode
 * and could not load on a dedicated server, which took the whole mod down before anything
 * registered. Keeping the client lookup in a client only class is what @OnlyIn was being asked to
 * do and cannot.
 */
public class ClientPacketSender {
    private ClientPacketSender() {
    }

    public static void sendToServer(AbstractPacket message) {
        if (Minecraft.getInstance().getConnection() != null) {
            ClientPacketDistributor.sendToServer(message);
        }
    }
}
