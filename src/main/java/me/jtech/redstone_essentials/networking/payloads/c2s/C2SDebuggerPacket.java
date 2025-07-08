package me.jtech.redstone_essentials.networking.payloads.c2s;

import me.jtech.redstone_essentials.Redstone_Essentials;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record C2SDebuggerPacket(int type) implements CustomPayload {
    public static final Id<C2SDebuggerPacket> ID = new Id<>(Redstone_Essentials.path("c2s_debugger_packet"));
    public static final PacketCodec<RegistryByteBuf, C2SDebuggerPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, C2SDebuggerPacket::type,
            C2SDebuggerPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public enum C2SInfoType {
        TOGGLE_FROZEN(0),
        STEP_BACK(1),
        STEP_FORWARD(2),
        TOGGLE_RECORDING(3),
        TOGGLE_PLAYBACK(4),
        TOGGLE_DEBUGGING(5);

        private final int id;

        C2SInfoType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static C2SInfoType fromId(int id) {
            for (C2SInfoType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return null;
        }
    }
}
