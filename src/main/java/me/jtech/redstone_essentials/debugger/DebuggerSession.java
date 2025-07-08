package me.jtech.redstone_essentials.debugger;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.jtech.redstone_essentials.Redstone_Essentials;
import me.jtech.redstone_essentials.utility.BlockChange;
import me.jtech.redstone_essentials.utility.BreakpointCondition;
import me.jtech.redstone_essentials.utility.TickSnapshot;
import me.jtech.redstone_essentials.utility.TileTickEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DebuggerSession {
    private static final Gson GSON = new GsonBuilder().create();

    private final ServerPlayerEntity owningPlayer;

    private final LinkedList<TickSnapshot> history = new LinkedList<>();
    private final Path savePath; // Path for saving snapshots to disk
    private final ServerWorld world; // World to apply changes to
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final int MAX_HISTORY_SIZE = 1000;

    private boolean isGameFrozen = false;

    private final PriorityQueue<TileTickEntry> pendingTileTicks = new PriorityQueue<>(Comparator.comparingInt(TileTickEntry::getDelay));
    private boolean steppingTileTicks = false;

    public final Map<BlockPos, Long> highlightedBlocks = new HashMap<>();

    private final Map<BlockPos, BreakpointCondition> blockBreakpoints = new HashMap<>();
    private final Set<Long> tickBreakpoints = new HashSet<>();

    private final Set<BlockPos> disabledBlockBreakpoints = new HashSet<>();
    private final Set<Long> disabledTickBreakpoints = new HashSet<>();

    private static boolean isReplaying = false;
    private static double replaySpeed = 1.0; // 1x speed by default
    private static long lastReplayTime = 0;

    private static boolean showAllTicks = false;
    private static BlockChange selectedBlock = null;

    public DebuggerSession(Path savePath, ServerWorld world, ServerPlayerEntity owningPlayer) {
        this.savePath = savePath;
        this.world = world;
        this.owningPlayer = owningPlayer;
    }

    public void startReplay() {
        if (!history.isEmpty()) {
            isReplaying = true;
            lastReplayTime = System.currentTimeMillis();
        }
    }

    public static void pauseReplay() {
        isReplaying = false;
    }

    public static void setReplaySpeed(double speed) {
        replaySpeed = speed;
    }

    public void rewindReplay() {
        isReplaying = false;
        revertToTick(history.getFirst().getTickNumber()); // Reset to first tick
    }

    public static boolean isShowAllTicks() {
        return showAllTicks;
    }

    public static void setShowAllTicks(boolean newShowAllTicks) {
        showAllTicks = newShowAllTicks;
    }

    public long getTickNumber() {
        return history.isEmpty() ? 0 : history.getLast().getTickNumber();
    }

    public void selectBlock(BlockChange change) {
        selectedBlock = change;
    }

    public BlockChange getSelectedBlock() {
        return selectedBlock;
    }

    public boolean hasSelectedBlock() {
        return selectedBlock != null;
    }

    public void tick() {
        if (isReplaying) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastReplayTime > (1000 / replaySpeed)) {
                stepForwardTick();
                lastReplayTime = currentTime;

                if (tickBreakpoints.contains(history.getLast().getTickNumber()) &&
                        !disabledTickBreakpoints.contains(history.getLast().getTickNumber())) {
                    pauseReplay();
                    System.out.println("Paused at breakpoint: Tick " + history.getLast().getTickNumber());
                }
            }
        }
    }

    public void setCurrentTick(long tick) {
        if (tick >= getMinTick() && tick <= getMaxTick()) {
            while (!history.isEmpty() && history.getLast().getTickNumber() > tick) {
                stepBack();
            }
            while (!history.isEmpty() && history.getLast().getTickNumber() < tick) {
                stepForwardTick();
            }
        }
    }

    public void recordChange(long tick, BlockChange change) {
        if (history.size() >= MAX_HISTORY_SIZE) {
            history.removeFirst(); // Remove oldest tick snapshot
        }
        if (history.isEmpty() || history.getLast().getTickNumber() != tick) {
            history.add(new TickSnapshot(tick));
        }
        history.getLast().addChange(new BlockChange(tick, change.getPosition(), change.getOldState(), change.getNewState()));
    }

    public void stepBack() {
        if (!history.isEmpty()) {
            TickSnapshot lastTick = history.removeLast();
            revertChanges(lastTick);
        }
    }

    public void saveToDisk() throws IOException {
        Files.writeString(savePath, serializeHistory());
    }

    public void loadFromDisk() throws IOException {
        String json = loadCompressed();
        deserializeHistory(json);
    }

    public void revertToTick(long tick) {
        while (!history.isEmpty() && history.getLast().getTickNumber() > tick) {
            stepBack();
        }
    }

    public void stepForwardTick() {
//        if (history.isEmpty() || getTickNumber() >= getMaxTick()) return; // No more ticks to step forward
//
//        TickSnapshot nextTick = history.get(history.indexOf(history.getLast()) + 1); // Get next tick
//        applyTickSnapshot(nextTick);
        Redstone_Essentials.getInstance().getServer().getTickManager().step(1);
    }
    
    int i = 1;
    public void stepBackwardTick() {
        revertToTick(history.getLast().getTickNumber()-i);
        i++;
    }

    private void applyTickSnapshot(TickSnapshot snapshot) {
        if (world == null) return;

        //RegistryWrapper.WrapperLookup registryLookup = world.getRegistryManager().getWrapperLookup(); // Get registry lookup

        for (BlockChange change : snapshot.getChanges()) {
            world.setBlockState(change.getPosition(), change.getNewState(), Block.NOTIFY_ALL);
        }
    }

    public long getMinTick() {
        return history.isEmpty() ? 0 : history.getFirst().getTickNumber();
    }

    public long getMaxTick() {
        return history.isEmpty() ? 0 : history.getLast().getTickNumber();
    }

    public void checkBlockBreakpoint(BlockPos pos, BlockState newState) {
        if (disabledBlockBreakpoints.contains(pos)) return; // Skip disabled breakpoints

        BreakpointCondition condition = blockBreakpoints.get(pos);
        if (condition != null && condition.matches(newState)) {
            pauseReplay();
            System.out.println("Breakpoint hit at " + pos);
        }
    }

    public void addBlockBreakpoint(BreakpointCondition condition) {
        blockBreakpoints.put(condition.getPosition(), condition);
    }

    public void addTickBreakpoint(long tick) {
        tickBreakpoints.add(tick);
    }

    public List<BreakpointCondition> getBlockBreakpoints() {
        return new ArrayList<>(blockBreakpoints.values());
    }

    public Set<Long> getTickBreakpoints() {
        return tickBreakpoints;
    }

    public void removeBlockBreakpoint(BreakpointCondition bp) {
        blockBreakpoints.remove(bp.getPosition());
    }

    public void removeTickBreakpoint(long tick) {
        tickBreakpoints.remove(tick);
    }

    public void jumpToBlock(BlockPos pos) {
        owningPlayer.teleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, true);
    }

    public void toggleBlockBreakpoint(BreakpointCondition bp) {
        if (disabledBlockBreakpoints.contains(bp.getPosition())) {
            disabledBlockBreakpoints.remove(bp.getPosition());
        } else {
            disabledBlockBreakpoints.add(bp.getPosition());
        }
    }

    public void toggleTickBreakpoint(long tick) {
        if (disabledTickBreakpoints.contains(tick)) {
            disabledTickBreakpoints.remove(tick);
        } else {
            disabledTickBreakpoints.add(tick);
        }
    }

    public boolean isBlockBreakpointEnabled(BreakpointCondition bp) {
        return !disabledBlockBreakpoints.contains(bp.getPosition());
    }

    public boolean isTickBreakpointEnabled(long tick) {
        return !disabledTickBreakpoints.contains(tick);
    }

    public List<BlockChange> getBlockChanges(boolean allTicks) {
        if (history.isEmpty()) {
            return Collections.emptyList();
        }
        if (allTicks) {
            return history.stream().flatMap(tick -> tick.getChanges().stream()).collect(Collectors.toList());
        } else {
            return history.getLast().getChanges();
        }
    }

    public void teleportTo(BlockChange change) {
        owningPlayer.teleport(change.getPosition().getX() + 0.5, change.getPosition().getY(), change.getPosition().getZ() + 0.5, false);
    }

    public void highlightBlock(BlockChange change) {
        highlightedBlocks.put(change.getPosition(), System.currentTimeMillis());
    }

    private void revertChanges(TickSnapshot snapshot) {
        for (BlockChange change : snapshot.getChanges()) {
            world.setBlockState(change.getPosition(), change.getOldState(), Block.NOTIFY_ALL);
        }
    }

    public void undoBlockChange(BlockChange change) {
        if (world == null) return;

        world.setBlockState(change.getPosition(), change.getOldState(), Block.NOTIFY_ALL);
    }

    private void deserializeHistory(String json) {
        Type listType = new TypeToken<LinkedList<TickSnapshot>>() {}.getType();
        history.clear();
        history.addAll(GSON.fromJson(json, listType));
    }

    public void clearHistory() {
        history.clear();
        try {
            Files.deleteIfExists(savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportSession() {
        //Path savePath = Paths.get(String.valueOf(FabricLoader.getInstance().getGameDir()), "debug_sessions", "session_" + System.currentTimeMillis() + ".json.gz");
        try {
            Files.createDirectories(savePath.getParent());
            String json = serializeHistory();
            saveCompressed(json, savePath);
            System.out.println("Debugging session exported: " + savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCompressed(String data, Path path) throws IOException {
        try (GZIPOutputStream gos = new GZIPOutputStream(Files.newOutputStream(path))) {
            gos.write(data.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void importSession(Path path) {
        try {
            String json = loadCompressed(path);
            deserializeHistory(json);
            System.out.println("Debugging session imported from: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String loadCompressed(Path path) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(Files.newInputStream(path))) {
            return new String(gis.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String serializeHistory() {
        return GSON.toJson(history);
    }

    private void saveCompressed() throws IOException {
        try (GZIPOutputStream gos = new GZIPOutputStream(Files.newOutputStream(savePath))) {
            gos.write(serializeHistory().getBytes(StandardCharsets.UTF_8));
        }
    }

    private String loadCompressed() throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(Files.newInputStream(savePath))) {
            return new String(gis.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public void asyncSave() {
        executor.submit(() -> {
            try {
                saveCompressed();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void recordTileTick(TileTickEntry entry) {
        pendingTileTicks.add(entry);
    }

    public void stepForwardTileTick(ServerWorld world) {
        if (!pendingTileTicks.isEmpty()) {
            TileTickEntry nextTick = pendingTileTicks.poll(); // Get next tile tick
            nextTick.execute(world); // Execute it manually
        }
    }

    public void enableTileTickStepping() { steppingTileTicks = true; }
    public void disableTileTickStepping() { steppingTileTicks = false; }
    public boolean isSteppingTileTicks() { return steppingTileTicks; }

    public void togglePause() {
        isGameFrozen = !isGameFrozen;
        Redstone_Essentials.getInstance().getServer().getTickManager().setFrozen(isGameFrozen);
    }

    public int getCurrentTick() { //TODO implement this
        return 0;
    }

    public List<TileTickEntry> getTileTicksForCurrentTick() { // TODO implement this
        return List.of();
    }
}

