package se.mickelus.mutil;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Start Minecraft far enough for its registries to answer.
 *
 * FML boots for these tests already, so classes load and registries fill. What it does not do is
 * bind item components, because NeoForge holds that back until a world loads so that a datapack can
 * still change them. Nothing here can build an ItemStack, and there is no honest way to fake it:
 * Item.components reads the same unbound holder, so binding it by hand is circular.
 *
 * That is a line, not a defect. Anything needing an item, an inventory or a world belongs in a
 * GameTest, which runs in a real server and gets the real answer. This covers the logic that does
 * not, which in this library is most of the gui maths and the data store.
 */
public class GameBootstrap implements BeforeAllCallback {
    private static boolean started;

    public static synchronized void start() {
        if (started) {
            return;
        }

        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Bootstrap.validate();
        started = true;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        start();
    }
}
