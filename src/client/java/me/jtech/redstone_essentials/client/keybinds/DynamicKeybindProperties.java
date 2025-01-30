package me.jtech.redstone_essentials.client.keybinds;

import java.util.List;

public class DynamicKeybindProperties {
    public String name; // the name of the keybind

    public String command; // the command to execute when the keybind is used

    /*
    If toggleInterval is enabled, the command in the 'command' string will be executed on a timer,
    specified in the 'intervalTime' variable.
    if toggleInterval AND cycleState are enabled for the command, it will cycle state every timer cycle.
    */
    public boolean hasToggleInterval; // the toggleInterval setting
    public long interval; // interval time in game ticks

    // If copyText is enabled, instead of sending the command, it will copy the value in the 'command' string to
    // your clipboard.
    // When used with cycleState or toggleState, it will copy the current state to your clipboard.
    public boolean copyText;
    public String copyTextMessage;

    // CycleState can have millions of commands to cycle between.
    // Holding the key and pressing a number key will jump to that cycle.
    // Pressing 1 and then 2 will jump to 12 for example. This means that if you type the wrong number, you have to let go of the key and
    // repeat the process.
    // Holding the key like this also won't trigger the action. (so long as holdKey isn't enabled)
    public boolean hasCycleState;
    public List<String> cycleStates;

    // If hold key is enabled, you will be able to hold the keybind, and it will execute the command every tick
    public boolean hasHoldKey;

    // Send toast just sends a toast to the screen when enabled with the provided title and message
    public boolean hasSendToast;
    public String toastTitle;
    public String toastMessage;

    private int currentCycleState = 0;
    private int currentInterval = 0;
    private boolean isIntervalToggled = false;

    public int getCurrentCycleState() {
        return currentCycleState;
    }

    public void setCurrentCycleState(int currentCycleState) {
        this.currentCycleState = currentCycleState;
    }

    public int getCurrentInterval() {
        return currentInterval;
    }

    public void setCurrentInterval(int currentInterval) {
        this.currentInterval = currentInterval;
    }

    public boolean isIntervalToggled() {
        return isIntervalToggled;
    }

    public void setIntervalToggled(boolean isIntervalToggled) {
        this.isIntervalToggled = isIntervalToggled;
    }
}
