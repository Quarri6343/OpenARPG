package quarri6343.openarpg.camera;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.network.NetworkEvent;
import quarri6343.openarpg.OpenARPG;
import quarri6343.openarpg.itempickup.ItemPickUpPacket;

import java.util.function.Supplier;

//テスト用の攻撃パケット
public class PlayerAttackPacket {
    private Entity target;
    private boolean isValid = false;

    public PlayerAttackPacket(Entity entity) {
        this.target = entity;
        this.isValid = true;
    }

    public PlayerAttackPacket() {
        this.isValid = false;
    }

    public static PlayerAttackPacket decode(FriendlyByteBuf buf) {
        PlayerAttackPacket packet = new PlayerAttackPacket();
        try {
            int entityID = buf.readInt();
            packet.target = OpenARPG.getEntityById(entityID);
        } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
            System.out.println("Exception while reading Packet: " + e);
            return packet;
        }
        packet.isValid = true;
        return packet;
    }

    public void encode(FriendlyByteBuf buf) {
        if (!isValid) return;
        buf.writeInt(this.target.getId());
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            sender.attack(target);
        });
        return true;
    }
}
