package se.mickelus.mutil.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import javax.annotation.Nullable;

/**
 * A slot that can be shown and hidden, for a container that pages several compartments through
 * one set of positions.
 *
 * <p>This was dropped when mutil moved off Forge, because it sat on the Forge item handler slot and
 * that package moved. Nothing in mutil itself used it, so its loss went unnoticed. Tetra does use
 * it, for the forged container's compartments and the workbench's material slots, so it was
 * restored here rather than worked around downstream.
 *
 * <p>It now extends {@link ResourceHandlerSlot} rather than the deprecated item handler slot. That
 * is not only about the deprecation: the old slot cast its handler to {@code IItemHandlerModifiable}
 * when setting a stack, so a menu built on the plain adapter died with a ClassCastException the
 * moment a server sent its contents, and the consumer had to write an adapter to avoid it. A
 * resource handler slot takes the handler and a modifier separately and asks for neither cast.
 */
public class ToggleableSlot extends ResourceHandlerSlot {

    private boolean isEnabled = true;

    /**
     * @param handler where the slot reads from
     * @param modifier how the slot writes back, usually {@code handler::set}
     * @param index the slot's index within the handler
     */
    public ToggleableSlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> modifier,
            int index, int xPosition, int yPosition) {
        super(handler, modifier, index, xPosition, yPosition);
    }

    public void toggle(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public boolean isActive() {
        return isEnabled;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return isEnabled;
    }

    @Override
    public boolean mayPlace(@Nullable ItemStack stack) {
        return isEnabled;
    }
}
