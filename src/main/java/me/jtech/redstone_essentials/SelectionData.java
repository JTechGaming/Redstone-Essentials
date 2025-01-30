package me.jtech.redstone_essentials;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;

public class SelectionData {
    public BlockPos blockPos;
    public Color color;
    public Vec3i size;
    public String label;
    public boolean isRTBO;
    public int offset = 1;
    public boolean isInverted = false;
    public int context;
    public int id;
    public String owningPlayer;
    public int base = 0;

    public SelectionData(BlockPos blockPos, Color color, Vec3i size, String label, boolean isRTBO, String owningPlayer) {
        this.blockPos = blockPos;
        this.color = color;
        this.size = size;
        this.label = label;
        this.isRTBO = isRTBO;
        this.owningPlayer = owningPlayer;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isRTBO() {
        return isRTBO;
    }

    public void setRTBO(boolean RTBO) {
        isRTBO = RTBO;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Vec3i getSize() {
        return size;
    }

    public void setSize(Vec3i size) {
        this.size = size;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public boolean isInverted() {
        return isInverted;
    }

    public void setInverted(boolean inverted) {
        isInverted = inverted;
    }

    public int getContext() {
        return context;
    }

    public void setContext(int context) {
        this.context = context;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOwningPlayer() {
        return owningPlayer;
    }

    public void setOwningPlayer(String owningPlayer) {
        this.owningPlayer = owningPlayer;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int selectionData) {
        this.base = selectionData;
    }

    public static final PacketCodec<PacketByteBuf, SelectionData> PACKET_CODEC = new PacketCodec<PacketByteBuf, SelectionData>() {
        public SelectionData decode(PacketByteBuf byteBuf) {
            BlockPos pos = byteBuf.readBlockPos();
            Color color = new Color(byteBuf.readInt(), true);
            Vector3f sizeIn = byteBuf.readVector3f();
            Vec3i sizeOut = new Vec3i((int) sizeIn.x, (int) sizeIn.y, (int) sizeIn.z);
            String label = byteBuf.readString();
            boolean isRTBO = byteBuf.readBoolean();
            int offset = byteBuf.readInt();
            boolean isInverted = byteBuf.readBoolean();
            int context = byteBuf.readInt();
            int id = byteBuf.readInt();
            String owningPlayer = byteBuf.readString();
            int base = byteBuf.readInt();

            SelectionData selectionData = new SelectionData(pos, color, sizeOut, label, isRTBO, owningPlayer);
            selectionData.setOffset(offset);
            selectionData.setInverted(isInverted);
            selectionData.setContext(context);
            selectionData.setId(id);
            selectionData.setBase(base);

            return selectionData;
        }

        public void encode(PacketByteBuf byteBuf, SelectionData selectionData) {
            byteBuf.writeBlockPos(selectionData.blockPos);
            byteBuf.writeInt(selectionData.color.getRGB());
            byteBuf.writeVector3f(new Vector3f(selectionData.size.getX(), selectionData.size.getY(), selectionData.size.getZ()));
            byteBuf.writeString(selectionData.label);
            byteBuf.writeBoolean(selectionData.isRTBO);
            byteBuf.writeInt(selectionData.offset);
            byteBuf.writeBoolean(selectionData.isInverted);
            byteBuf.writeInt(selectionData.context);
            byteBuf.writeInt(selectionData.id);
            byteBuf.writeString(selectionData.owningPlayer);
            byteBuf.writeInt(selectionData.base);
        }
    };

    public static final PacketCodec<PacketByteBuf, List<SelectionData>> LIST_PACKET_CODEC = PACKET_CODEC.collect(PacketCodecs.toList());
}
