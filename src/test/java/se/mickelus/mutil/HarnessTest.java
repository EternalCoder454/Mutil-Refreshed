package se.mickelus.mutil;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * That the harness itself works, which is worth one test of its own.
 *
 * If the registries are not up then every other test is measuring an empty game, and the first sign
 * of that should be this failing rather than something subtler failing later.
 */
@ExtendWith(GameBootstrap.class)
class HarnessTest {
    @Test
    void registriesAreUp() {
        assertNotNull(Items.IRON_INGOT);
        assertTrue(BuiltInRegistries.ITEM.size() > 100,
                "the item registry should be populated, found " + BuiltInRegistries.ITEM.size());
        assertNotNull(BuiltInRegistries.ITEM.getKey(Items.IRON_INGOT));
    }
}
