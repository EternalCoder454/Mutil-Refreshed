package se.mickelus.mutil.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nullable;

/**
 * A slot that can be shown and hidden, for a container that pages several compartments through
 * one set of positions.
 *
 * This was dropped when mutil moved off Forge, because it sat on the Forge item handler slot and
 * that package moved. Nothing in mutil itself used it, so its loss went unnoticed. Tetra does
 * use it, for the forged container's compartments, so it is restored here against the NeoForge
 * package rather than worked around downstream.
 */
public class ToggleableSlot extends SlotItemHandler {

    private boolean isEnabled = true;

    public ToggleableSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
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
