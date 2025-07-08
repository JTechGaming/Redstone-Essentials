package me.jtech.redstone_essentials.networking.payloads.s2c;

import me.jtech.redstone_essentials.Redstone_Essentials;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record S2CDebuggerPacket(int type) implements CustomPayload {
    public static final Id<S2CDebuggerPacket> ID = new Id<>(Redstone_Essentials.path("s2c_debugger_packet"));
    public static final PacketCodec<RegistryByteBuf, S2CDebuggerPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, S2CDebuggerPacket::type,
            S2CDebuggerPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public enum S2CInfoType {
        TEST(0);

        private final int id;

        S2CInfoType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static S2CInfoType fromId(int id) {
            for (S2CInfoType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return null;
        }
    }
}
