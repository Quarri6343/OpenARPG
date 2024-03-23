package quarri6343.openarpg.combat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;
import quarri6343.openarpg.OpenARPG;

import java.util.function.Supplier;

/**
 * テスト用のスキルパケット
 */
public class PlayerSkillPacket {
    
    @Nullable
    private Entity targetEntity;
    @Nullable
    private Vec3 location;
    private boolean isValid = false;
    private Skills skill;

    public PlayerSkillPacket(@Nullable Entity entity, @Nullable Vec3 location, Skills skill) {
        this.targetEntity = entity;
        this.location = location;
        this.skill = skill;
        this.isValid = true;
    }

    public PlayerSkillPacket() {
        this.isValid = false;
    }

    public static PlayerSkillPacket decode(FriendlyByteBuf buf) {
        PlayerSkillPacket packet = new PlayerSkillPacket();
        try {
            if(buf.readBoolean()) {
                packet.location = new Vec3(buf.readDouble(),buf.readDouble(),buf.readDouble());
            }
            
            if(buf.readBoolean()) {
                int entityID = buf.readInt();
                packet.targetEntity = OpenARPG.getEntityById(entityID);
            }
            
            int skillOrdinal = buf.readInt();
            packet.skill = Skills.values()[skillOrdinal];
        } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
            System.out.println("Exception while reading Packet: " + e);
            return packet;
        }
        packet.isValid = true;
        return packet;
    }

    public void encode(FriendlyByteBuf buf) {
        if (!isValid) return;
        
        if(location == null) {
            buf.writeBoolean(false);
        }
        else {
            buf.writeBoolean(true);
            buf.writeDouble(location.x);
            buf.writeDouble(location.y);
            buf.writeDouble(location.z);
        }
        
        if(targetEntity == null) {
            buf.writeBoolean(false);
        }
        else {
            buf.writeBoolean(true);
            buf.writeInt(targetEntity.getId());
        }
        
        buf.writeInt(skill.ordinal()); //TODO:スキルのenumにIDを設定してIDでスキルを管理するように
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender(); //TODO: 不正防止
            if(sender == null) {
                return;
            }
            
            //TODO: actionをSkillsのクラスに入れる
            if(skill == Skills.ATTACK) {
                sender.attack(targetEntity);
            } else if(skill == Skills.LIGHTNING_STRIKE) {
                if(location == null) {
                    return;
                }
                
                LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(sender.serverLevel());
                if (lightningbolt != null) {
                    lightningbolt.moveTo(location);
                    lightningbolt.setVisualOnly(true);
                    sender.serverLevel().addFreshEntity(lightningbolt);
                }
            }
            else if(skill == Skills.DODGE) {
                if(location == null) {
                    return;
                }
                
                Vec3 offset = location.subtract(new Vec3(sender.getX(), sender.getY(), sender.getZ()));
                offset = new Vec3(offset.x, 0, offset.z);
                offset = offset.normalize().scale(3f);
                sender.teleportTo(sender.getX() + offset.x, sender.getY() + offset.y, sender.getZ() + offset.z); //障害物を考慮していないが仮実装なので許して
            }
        });
        return true;
    }
}
