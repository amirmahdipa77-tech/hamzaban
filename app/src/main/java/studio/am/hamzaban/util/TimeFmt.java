package studio.am.hamzaban.util;

import android.content.Context;

import studio.am.hamzaban.R;

public final class TimeFmt {

    private TimeFmt() {
    }

    public static String relative(Context c, long ts) {
        long diff = Math.max(0L, System.currentTimeMillis() - ts) / 1000L;
        if (diff < 60) return c.getString(R.string.time_now);
        if (diff < 3600) return c.getString(R.string.time_min, Fa.n(c, diff / 60));
        if (diff < 86400) return c.getString(R.string.time_hour, Fa.n(c, diff / 3600));
        if (diff < 86400L * 30) return c.getString(R.string.time_day, Fa.n(c, diff / 86400));
        long days = diff / 86400;
        return c.getString(R.string.time_day, Fa.n(c, days));
    }
}
