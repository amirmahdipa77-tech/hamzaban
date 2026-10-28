package studio.am.hamzaban.util;

import android.content.Context;

public final class Fa {

    private static final char[] DIGITS = {'۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'};

    private Fa() {
    }

    public static String digits(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= '0' && ch <= '9') {
                b.append(DIGITS[ch - '0']);
            } else {
                b.append(ch);
            }
        }
        return b.toString();
    }

    public static String n(Context c, long v) {
        String s = String.valueOf(v);
        return LocaleHelper.isFa(c) ? digits(s) : s;
    }

    public static String n(Context c, int v) {
        return n(c, (long) v);
    }

    public static String num(Context c, String s) {
        return LocaleHelper.isFa(c) ? digits(s) : s;
    }

    public static String compact(Context c, long v) {
        String out;
        if (v >= 1000000L) {
            out = trim(v / 1000000.0) + "M";
        } else if (v >= 1000L) {
            out = trim(v / 1000.0) + "K";
        } else {
            out = String.valueOf(v);
        }
        if (!LocaleHelper.isFa(c)) return out;
        return digits(out).replace("K", "\u0647\u0632\u0627\u0631");
    }

    private static String trim(double v) {
        long rounded = Math.round(v);
        if (Math.abs(v - rounded) < 0.05) return String.valueOf(rounded);
        return String.format(java.util.Locale.US, "%.1f", v);
    }
}
