package se.mickelus.mutil.util;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class InventoryStream {
    /**
     * The stacks of a container, in slot order. Lazy, so a short circuiting operation stops reading
     * slots as soon as it has its answer.
     */
    public static Stream<ItemStack> of(Container inventory) {
        return IntStream.range(0, inventory.getContainerSize()).mapToObj(inventory::getItem);
    }
}
