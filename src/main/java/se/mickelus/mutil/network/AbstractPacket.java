package se.mickelus.mutil.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * AbstractPacket class. Should be the parent of all packets wishing to use the PacketHandler.
 *
 * Subclasses do not declare a payload type. Registering a packet derives one from the namespace
 * the PacketHandler was built with and the packet's own class name, and {@link #type()} looks it
 * up. The handler already knows both, so asking every packet to restate them was two dozen
 * copies of the same three lines per consumer, and forgetting one only showed up as an abstract
 * method error much later.
 *
 * @author sirgingalot, mickelus
 */
public abstract class AbstractPacket implements CustomPacketPayload {

    private static final Map<Class<? extends AbstractPacket>, CustomPacketPayload.Type<? extends AbstractPacket>> types =
            new ConcurrentHashMap<>();

    /**
     * Derive and remember the payload type for a packet class. Called by the PacketHandler when
     * the packet is registered.
     *
     * @param packetClass the class being registered
     * @param namespace   the namespace the PacketHandler was built with
     * @return the type for that class
     */
    @SuppressWarnings("unchecked")
    static <T extends AbstractPacket> CustomPacketPayload.Type<T> assignType(Class<T> packetClass, String namespace) {
        return (CustomPacketPayload.Type<T>) types.computeIfAbsent(packetClass,
                cls -> new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(namespace, pathFor(cls))));
    }

    /** {@return the class name as an identifier path, so WorkbenchPacketCraft is workbench_packet_craft} */
    private static String pathFor(Class<?> packetClass) {
        String name = packetClass.getSimpleName();
        StringBuilder path = new StringBuilder(name.length() + 8);
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                path.append('_');
            }
            path.append(Character.toLowerCase(c));
        }
        return path.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        CustomPacketPayload.Type<? extends AbstractPacket> type = types.get(getClass());
        if (type == null) {
            throw new IllegalStateException("Packet " + getClass().getName()
                    + " has no payload type. Register it with PacketHandler.registerPacket before it is sent.");
        }
        return type;
    }

    /**
     * Encode the packet data into the ByteBuf stream. Complex data sets may need specific data handlers (See @link{cpw.mods.fml.common.network.ByteBuffUtils})
     *
     * @param buffer the buffer to encode into
     */
    public abstract void toBytes(FriendlyByteBuf buffer);

    /**
     * Decode the packet data from the ByteBuf stream. Complex data sets may need specific data handlers (See @link{cpw.mods.fml.common.network.ByteBuffUtils})
     *
     * @param buffer the buffer to decode from
     */
    public abstract void fromBytes(FriendlyByteBuf buffer);

    /**
     * Handle the reception of this packet.
     *
     * @param player A reference to the sending player when handled on the server side
     */
    public abstract void handle(Player player);

    /**
     * Utility method that reads a string from a buffer object.
     * @param buffer The buffer containing the string to be read.
     * @return A string read from the buffer
     * @throws IOException
     */
    protected static String readString(FriendlyByteBuf buffer) throws IOException {
        String string = "";
        char c = buffer.readChar();

        while(c != '\0') {
            string += c;
            c = buffer.readChar();
        }

        return string;
    }

    protected static void writeString(String string, FriendlyByteBuf buffer) throws IOException {
        for (int i = 0; i < string.length(); i++) {
            buffer.writeChar(string.charAt(i));
        }
        buffer.writeChar('\0');
    }
}
