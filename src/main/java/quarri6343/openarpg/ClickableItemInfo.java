package quarri6343.openarpg;

import net.minecraft.world.entity.item.ItemEntity;

public record ClickableItemInfo(int minX, int minY, int maxX, int maxY, ItemEntity itemEntity) {
}
