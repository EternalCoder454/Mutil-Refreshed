package se.mickelus.mutil.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The stack shaped questions, asked of a resource handler.
 *
 * <p>A resource handler separates what is in a slot from how much of it there is, and moves things
 * inside a transaction. That is the right model, and it reads badly at a call site that only wants
 * to know what is in slot four:
 *
 * <pre>handler.getResource(4).toStack(handler.getAmountAsInt(4))</pre>
 *
 * <p>The deprecated item handler had a method for that, which is most of why code kept reaching for
 * it long after the capability had moved on. These are the same few lines, named, so a caller can
 * leave the old api behind without every read growing three method calls.
 *
 * <p>Each of these opens and commits its own transaction. That is right for a single change made on
 * its own and wrong inside a larger one, so anything that has to succeed or fail as a unit should
 * open a transaction and call the handler directly.
 */
@ParametersAreNonnullByDefault
public final class ResourceHandlers {

    private ResourceHandlers() {
    }

    /**
     * {@return what is in a slot, as a stack}
     */
    public static ItemStack stackIn(ResourceHandler<ItemResource> handler, int slot) {
        return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
    }

    /**
     * {@return true when no slot holds anything}
     */
    public static boolean isEmpty(ResourceHandler<ItemResource> handler) {
        for (int slot = 0; slot < handler.size(); slot++) {
            if (!handler.getResource(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Take up to count items out of a slot.
     *
     * @return what came out, which may be less than asked for and may be empty
     */
    public static ItemStack extract(ResourceHandler<ItemResource> handler, int slot, int count) {
        ItemResource resource = handler.getResource(slot);
        if (resource.isEmpty() || count <= 0) {
            return ItemStack.EMPTY;
        }

        try (Transaction transaction = Transaction.openRoot()) {
            int taken = handler.extract(slot, resource, count, transaction);
            transaction.commit();
            return taken > 0 ? resource.toStack(taken) : ItemStack.EMPTY;
        }
    }

    /**
     * Put as much of a stack into a slot as will fit.
     *
     * @return what would not fit, which is the whole stack when none of it did
     */
    public static ItemStack insert(ResourceHandler<ItemResource> handler, int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        try (Transaction transaction = Transaction.openRoot()) {
            int accepted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), transaction);
            transaction.commit();
            return accepted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - accepted);
        }
    }

    /**
     * {@return whether a slot would accept this stack, ignoring what is already in it}
     */
    public static boolean isValid(ResourceHandler<ItemResource> handler, int slot, ItemStack stack) {
        return handler.isValid(slot, ItemResource.of(stack));
    }

    /**
     * {@return how much of this stack a slot can hold}
     */
    public static int capacity(ResourceHandler<ItemResource> handler, int slot, ItemStack stack) {
        return handler.getCapacityAsInt(slot, ItemResource.of(stack));
    }
}
