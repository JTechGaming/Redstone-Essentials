package me.jtech.redstone_essentials.networking;

public class InfoPackets {
    public enum S2C {
        CLIENT_RECEIVE_SERVER_VERSION(0),
        RW_BIN(1),
        CLEAR_PINGS(2),
        SEND_PINGS_TO_NEW_CLIENT(3),
        CLIENT_RECEIVE_NEW_CLIENT_PINGS(4);

        private final int value;
        S2C(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum C2S {
        FINALISE_BITMAP(0),
        FORCE_NEIGHBOUR_UPDATES(1),
        ASK_SERVER_MOD_VERSION(2),
        RETURN_RW_BIN(3),
        CLEAR_PINGS(4),
        SEND_PINGS_TO_NEW_CLIENT(5);

        private final int value;
        C2S(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static int getInt(S2C enumValue) {
        return enumValue.getValue();
    }

    public static int getInt(C2S enumValue) {
        return enumValue.getValue();
    }

    public static S2C getS2CEnum(int value) {
        return S2C.values()[value];
    }

    public static C2S getC2SEnum(int value) {
        return C2S.values()[value];
    }
}
