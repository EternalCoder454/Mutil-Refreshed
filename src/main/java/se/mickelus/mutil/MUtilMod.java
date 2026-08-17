package se.mickelus.mutil;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(MUtilMod.MOD_ID)
@EventBusSubscriber(modid = MUtilMod.MOD_ID)
public class MUtilMod {
    public static final String MOD_ID = "mutil";

    public MUtilMod(ModContainer container) {
        ConfigHandler.setup(container);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        Perks.init(Minecraft.getInstance().getUser().getProfileId().toString());
    }
}
