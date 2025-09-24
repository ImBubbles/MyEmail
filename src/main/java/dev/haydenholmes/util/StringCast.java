package dev.haydenholmes.util;

import java.util.Base64;

public final class StringCast {

    public static int toInt(String str, int def) {
        try {
            return Integer.parseInt(str);
        } catch(NumberFormatException e) {
            return def;
        }
    }

    public static Integer toInteger(String str) {
        try {
            return Integer.valueOf(str);
        } catch(NumberFormatException e) {
            return null;
        }
    }

    public static String toBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    public static String fromBase64(String str) {
        byte[] bytes = Base64.getDecoder().decode(str);
        return new String(bytes);
    }

}