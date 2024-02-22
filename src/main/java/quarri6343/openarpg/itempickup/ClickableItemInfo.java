package quarri6343.openarpg.itempickup;

import net.minecraft.world.entity.item.ItemEntity;

/**
 * クリック可能なドロップアイテムのgui情報
 */
public record ClickableItemInfo(int minX, int minY, int maxX, int maxY, ItemEntity itemEntity) {
}
