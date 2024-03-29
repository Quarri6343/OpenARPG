package quarri6343.openarpg;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import quarri6343.openarpg.combat.DebugMonsterSpawnPacket;
import quarri6343.openarpg.combat.PlayerSkillPacket;
import quarri6343.openarpg.itempickup.ItemPickUpPacket;

import java.util.Optional;

public class Network {

    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("openarpg", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        INSTANCE.registerMessage(id(), ItemPickUpPacket.class, ItemPickUpPacket::encode, ItemPickUpPacket::decode,
                ItemPickUpPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(id(), PlayerSkillPacket.class, PlayerSkillPacket::encode, PlayerSkillPacket::decode,
                PlayerSkillPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(id(), DebugMonsterSpawnPacket.class, DebugMonsterSpawnPacket::encode, DebugMonsterSpawnPacket::decode,
                DebugMonsterSpawnPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
