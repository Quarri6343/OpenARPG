package quarri6343.openarpg;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ItemPickUpPacket {
    private ItemEntity itemEntity;
    private boolean isValid = false;

    public ItemPickUpPacket(ItemEntity entity) {
        this.itemEntity = entity;
        this.isValid = true;
    }
    public ItemPickUpPacket() {
        this.isValid = false;
    }
    
    public static ItemPickUpPacket decode(FriendlyByteBuf buf) {
        ItemPickUpPacket packet = new ItemPickUpPacket();
        try {
            int entityID = buf.readInt();
            packet.itemEntity = getEntityById(entityID);
        } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
            System.out.println("Exception while reading Packet: " + e);
            return packet;
        }
        packet.isValid = true;
        return packet;
    }

    public void encode(FriendlyByteBuf buf) {
        if (!isValid) return;
        buf.writeInt(this.itemEntity.getId());
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            playerTouch(itemEntity, sender);
        });
        return true;
    }

    public static ItemEntity getEntityById(int entityId) {
        for (ServerLevel world : OpenARPG.getServer().getAllLevels()) {
            Entity entity = world.getEntity(entityId);
            if(entity instanceof ItemEntity){
                return (ItemEntity)entity;
            }
        }
        return null;
    }

    /**
     * アイテムの拾得をエミュレート
     * 
     * @param itemEntity アイテムエンティティ
     * @param pEntity 拾うプレイヤー
     */
    public void playerTouch(ItemEntity itemEntity, Player pEntity) {
        ItemStack itemstack = itemEntity.getItem();
        Item item = itemstack.getItem();
        int i = itemstack.getCount();
        ItemStack copy = itemstack.copy();
        if (i <= 0 || pEntity.getInventory().add(itemstack)) {
            i = copy.getCount() - itemstack.getCount();
            copy.setCount(i);
            net.minecraftforge.event.ForgeEventFactory.firePlayerItemPickupEvent(pEntity, itemEntity, copy);
            pEntity.take(itemEntity, i);
            if (itemstack.isEmpty()) {
                itemEntity.discard();
                itemstack.setCount(i);
            }

            pEntity.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
            pEntity.onItemPickup(itemEntity);
        }
    }
}
