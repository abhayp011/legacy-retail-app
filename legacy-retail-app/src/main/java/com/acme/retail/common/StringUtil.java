package com.acme.retail.common;

import java.security.MessageDigest;

public class StringUtil {

    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    /** MD5 hex digest (used for stored passwords). */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] d = md.digest(input.getBytes("UTF-8"));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < d.length; i++) {
                String h = Integer.toHexString(0xff & d[i]);
                if (h.length() == 1) sb.append('0');
                sb.append(h);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Minimal JSON string escaping. */
    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Money rounding used everywhere: half-up to 2 decimals on a double. */
    public static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
