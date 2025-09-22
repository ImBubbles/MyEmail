package dev.haydenholmes.util;

public final class StringCast {

    public static int toInt(String val, int def) {
        try {
            return Integer.parseInt(val);
        } catch(NumberFormatException e) {
            return def;
        }
    }

    public static Integer toInteger(String val) {
        try {
            return Integer.valueOf(val);
        } catch(NumberFormatException e) {
            return null;
        }
    }

}