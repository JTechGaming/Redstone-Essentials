package me.jtech.redstone_essentials.utility;

import java.util.HashMap;
import java.util.Map;


public class SelectionContext {
    public static Map<Integer, IClientSelectionContext> contexts = new HashMap<>();

    public static int register(IClientSelectionContext context) {
        contexts.put(contexts.size(), context);
        return contexts.size()-1;
    }

    public static IClientSelectionContext get(int context) {
        return contexts.get(context);
    }
}
