package se.mickelus.mutil;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
class ConfigHandler {
    public static Client client;
    static ModConfigSpec clientSpec;

    public static void setup(ModContainer container) {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            setupClient();
            container.registerConfig(ModConfig.Type.CLIENT, clientSpec);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void setupClient() {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        client = new Client(builder);
        clientSpec = builder.build();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client {
        public ModConfigSpec.BooleanValue queryPerks;

        Client(ModConfigSpec.Builder builder) {
            queryPerks = builder
                    .comment("Controls if perks data should be queried on startup")
                    .define("query_perks", true);
        }
    }
}
