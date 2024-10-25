package me.jtech.redstonecomptools.client.rendering.screen.keybinds;

import java.util.List;

public class KeybindEntry { //TODO comment this
    private String name;
    private String command;
    private List<Integer> key;
    private boolean shiftRequired;
    private boolean ctrlRequired;

    public KeybindEntry(String name, String command, List<Integer> key, boolean shiftRequired, boolean ctrlRequired) {
        this.name = name;
        this.command = command;
        this.key = key;
        this.shiftRequired = shiftRequired;
        this.ctrlRequired = ctrlRequired;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public List<Integer> getKey() { return key; }
    public void setKey(List<Integer> key) { this.key = key; }

    public boolean isShiftRequired() { return shiftRequired; }
    public void setShiftRequired(boolean shiftRequired) { this.shiftRequired = shiftRequired; }

    public boolean isCtrlRequired() { return ctrlRequired; }
    public void setCtrlRequired(boolean ctrlRequired) { this.ctrlRequired = ctrlRequired; }
}

