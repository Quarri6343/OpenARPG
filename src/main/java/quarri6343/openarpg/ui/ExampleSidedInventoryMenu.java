package quarri6343.openarpg.ui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import quarri6343.openarpg.OpenARPG;

public class ExampleSidedInventoryMenu extends AbstractContainerMenu {

    // Client Constructor
    public ExampleSidedInventoryMenu(int containerId, Inventory playerInv, FriendlyByteBuf additionalData) {
        this(containerId, playerInv, new ItemStackHandler(3));
    }

    // Server Constructor
    public ExampleSidedInventoryMenu(int containerId, Inventory playerInv, IItemHandler dataInventory) {
        super(OpenARPG.EXAMPLE_SIDED_INVENTORY_MENU.get(), containerId);

        createPlayerHotbar(playerInv);
        createPlayerInventory(playerInv);
        
        addSlot(new SlotItemHandler(dataInventory, 0, 44, 36));
        addSlot(new SlotItemHandler(dataInventory, 1, 80, 36));
        addSlot(new SlotItemHandler(dataInventory, 2, 116, 36));
//        createBlockEntityInventory(be);
    }

    private void createPlayerInventory(Inventory playerInv) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInv,
                        9 + column + (row * 9),
                        8 + (column * 18),
                        84 + (row * 18)));
            }
        }
    }

    private void createPlayerHotbar(Inventory playerInv) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInv,
                    column,
                    8 + (column * 18),
                    142));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        Slot fromSlot = getSlot(pIndex);
        ItemStack fromStack = fromSlot.getItem();

        if (fromStack.getCount() <= 0)
            fromSlot.set(ItemStack.EMPTY);

        if (!fromSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack copyFromStack = fromStack.copy();

        if (pIndex < 36) {
            // We are inside of the player's inventory
            if (!moveItemStackTo(fromStack, 36, 39, false))
                return ItemStack.EMPTY;
        } else if (pIndex < 39) {
            // We are inside of the block entity inventory
            if (!moveItemStackTo(fromStack, 0, 36, false))
                return ItemStack.EMPTY;
        } else {
            System.err.println("Invalid slot index: " + pIndex);
            return ItemStack.EMPTY;
        }

        fromSlot.setChanged();
        fromSlot.onTake(pPlayer, fromStack);

        return copyFromStack;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
//        return stillValid(this.levelAccess, pPlayer, BlockInit.EXAMPLE_SIDED_INVENTORY_BLOCK.get());
        return true;
    }
}
