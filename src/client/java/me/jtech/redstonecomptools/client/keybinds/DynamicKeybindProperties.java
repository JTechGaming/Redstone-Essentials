package me.jtech.redstonecomptools.client.keybinds;

import java.util.ArrayList;
import java.util.List;

public class DynamicKeybindProperties {
    public String command; // the command to execute when the keybind is used

    // If toggleState is enabled, the 'command' string won't be used.
    // Instead, tState1 and tState 2 will be used depending on the current toggle state.
    // State handling is done in the DynamicKeybindHandler class.
    public boolean hasToggleState; // the toggleState setting
    public String tState1; // toggle state 1
    public String tState2; // toggle state 2

    /*
    If toggleInterval is enabled, the command in the 'command' string will be executed on a timer,
    specified in the 'intervalTime' variable.
    if toggleInterval AND toggleState are enabled for the command, it will toggle state every timer cycle.
    */
    public boolean hasToggleInterval; // the toggleInterval setting
    public long interval; // interval time in game ticks

    // If copyText is enabled, instead of sending the command, it will copy the value in the 'command' string to
    // your clipboard.
    // When used with toggleState
    public boolean copyText;

    // CycleState does the same thing toggleState does, but isnt limited to just 2 commands to cycle between, but can have
    // millions of commands to cycle between.
    // Holding the key and pressing a number key will jump to that cycle.
    // Pressing 1 and then 2 will jump to 12 for example. This means that if you type the wrong number, you have to let go of the key and
    // repeat the process.
    // Holding the key like this also won't trigger the action. (so long as holdKey isn't enabled)
    public boolean hasCycleState;
    public List<String> cycleStates;

    public boolean hasHoldKey;
}
