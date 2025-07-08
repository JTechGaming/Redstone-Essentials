package me.jtech.redstone_essentials.client.debugger;

import me.jtech.redstone_essentials.networking.payloads.c2s.C2SDebuggerPacket;
import me.jtech.redstone_essentials.utility.BlockChange;
import me.jtech.redstone_essentials.utility.BreakpointCondition;
import me.jtech.redstone_essentials.utility.TileTickEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.BlockPos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientDebugSession {
    public boolean isActive = false;

    public void jumpToBlock(BlockPos position) {

    }

    public void teleportTo(BlockChange change) {
    }

    public void highlightBlock(BlockChange change) {

    }

    public void addBlockBreakpoint(BreakpointCondition condition) {

    }

    public void revertToTick(long tickNumber) {

    }

    public void undoBlockChange(BlockChange change) {

    }

    public void selectBlock(BlockChange change) {

    }

    public boolean isShowAllTicks() {
        return false;
    }

    public List<BlockChange> getBlockChanges(boolean showAllTicks) {
        return new ArrayList<>();
    }

    public void togglePause() {
        ClientPlayNetworking.send(new C2SDebuggerPacket(C2SDebuggerPacket.C2SInfoType.TOGGLE_FROZEN.getId()));
    }

    public void stepForwardTick() {
        ClientPlayNetworking.send(new C2SDebuggerPacket(C2SDebuggerPacket.C2SInfoType.STEP_FORWARD.getId()));
    }

    public void stepBackwardTick() {
        ClientPlayNetworking.send(new C2SDebuggerPacket(C2SDebuggerPacket.C2SInfoType.STEP_BACK.getId()));
    }

    public void startReplay() {
        ClientPlayNetworking.send(new C2SDebuggerPacket(C2SDebuggerPacket.C2SInfoType.TOGGLE_PLAYBACK.getId()));
    }

    public void pauseReplay() {

    }

    public void setReplaySpeed(double v) {

    }

    public void rewindReplay() {

    }

    public long getMinTick() {
        return 0;
    }

    public long getMaxTick() {
        return 0;
    }

    public void setCurrentTick(long tick) {

    }

    public int getCurrentTick() {
        return 0;
    }

    public List<TileTickEntry> getTileTicksForCurrentTick() {
        return new ArrayList<>();
    }

    public void addTickBreakpoint(int delay) {

    }

    public boolean hasSelectedBlock() {
        return false;
    }

    public BlockChange getSelectedBlock() {
        return null;
    }

    public void exportSession() {

    }

    public void importSession(Path of) {

    }

    public List<BreakpointCondition> getBlockBreakpoints() {
        return new ArrayList<>();
    }

    public boolean isBlockBreakpointEnabled(BreakpointCondition bp) {
        return false;
    }

    public List<Long> getTickBreakpoints() {
        return new ArrayList<>();
    }

    public boolean isTickBreakpointEnabled(Long tick) {
        return false;
    }

    public void toggleBlockBreakpoint(BreakpointCondition selectedBlockBreakpoint) {

    }

    public void toggleTickBreakpoint(Long selectedTickBreakpoint) {

    }

    public void removeBlockBreakpoint(BreakpointCondition selectedBlockBreakpoint) {

    }

    public void removeTickBreakpoint(Long selectedTickBreakpoint) {

    }
}
