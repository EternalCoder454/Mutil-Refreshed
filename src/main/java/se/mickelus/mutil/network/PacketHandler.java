package se.mickelus.mutil.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class PacketHandler {
    private static final Logger logger = LogManager.getLogger();

    private final String version;
    private final ArrayList<PacketRegistration<? extends AbstractPacket>> registrations = new ArrayList<>();

    public PacketHandler(String namespace, String channelId, String protocolVersion) {
        this.version = protocolVersion;
    }

    /**
     * Register your packet with the pipeline. Discriminators are automatically set.
     * Call this before {@link RegisterPayloadHandlersEvent} fires.
     *
     * @param packetClass the class to register
     * @param supplier    A supplier returning a new instance of packetClass (used for decoding)
     * @return whether registration was successful
     */
    public <T extends AbstractPacket> boolean registerPacket(Class<T> packetClass, Supplier<T> supplier) {
        if (registrations.size() > 256) {
            logger.warn("Attempted to register packet but packet list is full: " + packetClass);
            return false;
        }

        for (PacketRegistration<?> reg : registrations) {
            if (reg.packetClass == packetClass) {
                logger.warn("Attempted to register packet but packet is already in list: " + packetClass);
                return false;
            }
        }

        registrations.add(new PacketRegistration<>(packetClass, supplier));
        return true;
    }

    /**
     * Call this from a {@link RegisterPayloadHandlersEvent} handler on the mod event bus to
     * register all packets that were added via {@link #registerPacket}.
     */
    @SuppressWarnings("unchecked")
    public void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(version);
        for (PacketRegistration<?> reg : registrations) {
            registerOne(registrar, (PacketRegistration<AbstractPacket>) reg);
        }
    }

    private <T extends AbstractPacket> void registerOne(PayloadRegistrar registrar, PacketRegistration<T> reg) {
        StreamCodec<FriendlyByteBuf, T> codec = StreamCodec.of(
                (buf, packet) -> packet.toBytes(buf),
                buf -> {
                    T packet = reg.supplier.get();
                    packet.fromBytes(buf);
                    return packet;
                }
        );
        registrar.playBidirectional(reg.type(), codec, this::onMessage);
    }

    private <T extends AbstractPacket> void onMessage(T message, IPayloadContext ctx) {
        ctx.enqueueWork(() -> message.handle(ctx.player()));
    }

    @OnlyIn(Dist.CLIENT)
    private Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public void sendTo(AbstractPacket message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public void sendToAllPlayers(AbstractPacket message) {
        PacketDistributor.sendToAllPlayers(message);
    }

    public void sendToAllPlayersNear(AbstractPacket message, ServerLevel level, double x, double y, double z, double radius) {
        PacketDistributor.sendToPlayersNear(level, null, x, y, z, radius, message);
    }

    /** Convenience overload using BlockPos */
    public void sendToAllPlayersNear(AbstractPacket message, ServerLevel level, BlockPos pos, double radius) {
        sendToAllPlayersNear(message, level, pos.getX(), pos.getY(), pos.getZ(), radius);
    }

    @OnlyIn(Dist.CLIENT)
    public void sendToServer(AbstractPacket message) {
        if (Minecraft.getInstance().getConnection() != null) {
            PacketDistributor.sendToServer(message);
        }
    }

    /**
     * Holds the class, supplier, and derived payload Type for a registered packet.
     */
    private static class PacketRegistration<T extends AbstractPacket> {
        final Class<T> packetClass;
        final Supplier<T> supplier;
        private CustomPacketPayload.Type<T> cachedType;

        PacketRegistration(Class<T> packetClass, Supplier<T> supplier) {
            this.packetClass = packetClass;
            this.supplier = supplier;
        }

        @SuppressWarnings("unchecked")
        CustomPacketPayload.Type<T> type() {
            if (cachedType == null) {
                cachedType = (CustomPacketPayload.Type<T>) supplier.get().type();
            }
            return cachedType;
        }
    }
}
