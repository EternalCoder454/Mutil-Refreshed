package se.mickelus.mutil.scheduling;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ClientScheduler extends AbstractScheduler {
    @SubscribeEvent
    public void onClientLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) {
            tick();
        }
    }
}
