package quarri6343.openarpg.combat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * 任意のモンスターを沸かせるパケット
 */
public class DebugMonsterSpawnPacket {
    private String target;
    private Vector3f spawnPos;
    private boolean isValid = false;

    public DebugMonsterSpawnPacket(String entityID, Vector3f spawnPos) {
        this.target = entityID;
        this.spawnPos = spawnPos;
        this.isValid = true;
    }

    public DebugMonsterSpawnPacket() {
        this.isValid = false;
    }

    public static DebugMonsterSpawnPacket decode(FriendlyByteBuf buf) {
        DebugMonsterSpawnPacket packet = new DebugMonsterSpawnPacket();
        try {
            packet.target = buf.readUtf();
            packet.spawnPos = buf.readVector3f();
        } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
            System.out.println("Exception while reading Packet: " + e);
            return packet;
        }
        packet.isValid = true;
        return packet;
    }

    public void encode(FriendlyByteBuf buf) {
        if (!isValid) return;
        buf.writeUtf(this.target);
        buf.writeVector3f(spawnPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender(); 
            if(sender.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {
                spawnEntity(target, sender.serverLevel(), new Vec3(spawnPos));//TODO: 高度な不正防止
            }
        });
        return true;
    }

    public static void spawnEntity(String entityRegistryName, Level level, Vec3 pos) {
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityRegistryName));
        if (entityType != null) {
            Entity entity = entityType.create(level);
            if (entity != null) {
                entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                entity.setPos(pos);
                level.addFreshEntity(entity);
            }
        }
    }
}
