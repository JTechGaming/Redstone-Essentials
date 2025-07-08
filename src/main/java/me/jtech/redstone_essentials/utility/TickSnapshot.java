package me.jtech.redstone_essentials.utility;

import java.util.ArrayList;
import java.util.List;

public class TickSnapshot {
    private final long tickNumber;
    private final List<BlockChange> changes;

    public TickSnapshot(long tickNumber) {
        this.tickNumber = tickNumber;
        this.changes = new ArrayList<>();
    }

    public void addChange(BlockChange change) {
        changes.add(change);
    }

    public long getTickNumber() {
        return tickNumber;
    }

    public List<BlockChange> getChanges() {
        return changes;
    }
}

