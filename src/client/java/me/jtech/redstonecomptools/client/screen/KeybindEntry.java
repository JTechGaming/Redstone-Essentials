package me.jtech.redstonecomptools.client.screen;

public class KeybindEntry {
    private String name;
    private String command;
    private boolean shiftRequired;
    private boolean ctrlRequired;

    public KeybindEntry(String name, String command, boolean shiftRequired, boolean ctrlRequired) {
        this.name = name;
        this.command = command;
        this.shiftRequired = shiftRequired;
        this.ctrlRequired = ctrlRequired;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public boolean isShiftRequired() { return shiftRequired; }
    public void setShiftRequired(boolean shiftRequired) { this.shiftRequired = shiftRequired; }

    public boolean isCtrlRequired() { return ctrlRequired; }
    public void setCtrlRequired(boolean ctrlRequired) { this.ctrlRequired = ctrlRequired; }
}

