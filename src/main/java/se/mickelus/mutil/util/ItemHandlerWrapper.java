package se.mickelus.mutil.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A {@link Container} view over a resource handler, for the vanilla apis that still want one.
 *
 * <p>Loot tables are the reason this exists. {@code LootTable.fill} takes a Container and there is
 * no resource handler equivalent, so a block that stores its contents as resources needs this to be
 * filled from a loot table at all.
 *
 * <p>This used to wrap {@code IItemHandler}, which is deprecated and marked for removal. That
 * mattered more than a warning: consumers had already grown an adapter to hand one over, because
 * {@code IItemHandler.of} yields a read and transfer view alone and the write half had to be
 * bolted back on. Taking the resource handler directly means the thing that owns the items is
 * passed straight in.
 *
 * <p><b>Writes need a modifier.</b> A resource handler can insert and extract, but it cannot set a
 * slot to an arbitrary stack, which is what {@code setItem} means. {@code StacksResourceHandler}
 * and anything else that stores slots offers {@code set(int, T, int)}, which matches
 * {@link IndexModifier} exactly, so a handler is usually its own modifier and can be passed twice.
 * Where there is no modifier, use the single argument constructor and the container becomes read
 * only rather than silently dropping writes.
 */
@ParametersAreNonnullByDefault
public class ItemHandlerWrapper implements Container {

    protected final ResourceHandler<ItemResource> inv;

    /**
     * The modifier used by {@code setItem}, or null when this view is read only.
     */
    protected final IndexModifier<ItemResource> modifier;

    /**
     * A read only view. {@code setItem} and the removals that depend on it do nothing.
     */
    public ItemHandlerWrapper(ResourceHandler<ItemResource> inv) {
        this(inv, null);
    }

    /**
     * A writable view. Pass the handler as its own modifier when it stores slots, which is the
     * usual case: {@code new ItemHandlerWrapper(handler, handler::set)}.
     */
    public ItemHandlerWrapper(ResourceHandler<ItemResource> inv, IndexModifier<ItemResource> modifier) {
        this.inv = inv;
        this.modifier = modifier;
    }

    @Override
    public int getContainerSize() {
        return inv.size();
    }

    @Override
    public ItemStack getItem(int slot) {
        return inv.getResource(slot).toStack(inv.getAmountAsInt(slot));
    }

    /**
     * Take at most count items out of a slot and hand them back.
     *
     * <p>The old version split the stack it was given, which worked only because the item handler
     * handed out a live reference. A resource handler hands out a value, so splitting it changed
     * nothing in the inventory. This extracts, which is what the method has always claimed to do.
     */
    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemResource resource = inv.getResource(slot);
        if (resource.isEmpty() || count <= 0) {
            return ItemStack.EMPTY;
        }

        try (Transaction transaction = Transaction.openRoot()) {
            int taken = inv.extract(slot, resource, count, transaction);
            transaction.commit();
            return taken > 0 ? resource.toStack(taken) : ItemStack.EMPTY;
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (modifier != null) {
            modifier.set(slot, ItemResource.of(stack), stack.getCount());
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack existing = getItem(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        setItem(slot, ItemStack.EMPTY);
        return existing;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < inv.size(); i++) {
            if (!inv.getResource(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return inv.isValid(slot, ItemResource.of(stack));
    }

    @Override
    public void clearContent() {
        try (Transaction transaction = Transaction.openRoot()) {
            for (int i = 0; i < inv.size(); i++) {
                ItemResource resource = inv.getResource(i);
                if (!resource.isEmpty()) {
                    inv.extract(i, resource, inv.getAmountAsInt(i), transaction);
                }
            }
            transaction.commit();
        }
    }

    // The following are never used by vanilla in crafting, and are defunct as mods need not
    // override them.
    @Override
    public int getMaxStackSize() {
        return 0;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void startOpen(ContainerUser user) {
    }

    @Override
    public void stopOpen(ContainerUser user) {
    }
}
