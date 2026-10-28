package studio.am.hamzaban.ui.widget;

import java.util.HashMap;
import java.util.Map;

import studio.am.hamzaban.R;

/** Maps semantic glyph names to the bundled Octicons drawables. */
public final class Icons {

    private static final Map<String, Integer> MAP = new HashMap<>();

    static {
        MAP.put("translate", R.drawable.oct_globe);
        MAP.put("globe", R.drawable.oct_globe);
        MAP.put("language", R.drawable.oct_globe);
        MAP.put("swap_horiz", R.drawable.oct_arrow_switch);
        MAP.put("swap_vert", R.drawable.oct_arrow_switch);
        MAP.put("content_copy", R.drawable.oct_copy);
        MAP.put("copy", R.drawable.oct_copy);
        MAP.put("duplicate", R.drawable.oct_duplicate);
        MAP.put("share", R.drawable.oct_share);
        MAP.put("bookmark", R.drawable.oct_bookmark);
        MAP.put("history", R.drawable.oct_history);
        MAP.put("clock", R.drawable.oct_clock);
        MAP.put("settings", R.drawable.oct_gear);
        MAP.put("tune", R.drawable.oct_gear);
        MAP.put("delete", R.drawable.oct_trash);
        MAP.put("close", R.drawable.oct_x);
        MAP.put("check", R.drawable.oct_check);
        MAP.put("check_circle", R.drawable.oct_check_circle);
        MAP.put("done_all", R.drawable.oct_check_circle);
        MAP.put("search", R.drawable.oct_search);
        MAP.put("chevron_right", R.drawable.oct_chevron_right);
        MAP.put("chevron_left", R.drawable.oct_arrow_left);
        MAP.put("arrow_forward", R.drawable.oct_arrow_right);
        MAP.put("arrow_back", R.drawable.oct_arrow_left);
        MAP.put("download", R.drawable.oct_download);
        MAP.put("download_done", R.drawable.oct_download);
        MAP.put("star", R.drawable.oct_star);
        MAP.put("add", R.drawable.oct_plus);
        MAP.put("dark_mode", R.drawable.oct_moon);
        MAP.put("light_mode", R.drawable.oct_sun);
        MAP.put("info", R.drawable.oct_info);
        MAP.put("offline_bolt", R.drawable.oct_zap);
        MAP.put("auto_awesome", R.drawable.oct_rocket);
        MAP.put("volume_up", R.drawable.oct_unmute);
        MAP.put("record_voice_over", R.drawable.oct_unmute);
        MAP.put("refresh", R.drawable.oct_sync);
        MAP.put("autorenew", R.drawable.oct_sync);
        MAP.put("edit", R.drawable.oct_pencil);
        MAP.put("more_vert", R.drawable.oct_kebab_horizontal);
        MAP.put("code", R.drawable.oct_code);
        MAP.put("repo", R.drawable.oct_repo);
        MAP.put("shield", R.drawable.oct_shield_check);
        MAP.put("link", R.drawable.oct_link);
        MAP.put("eye", R.drawable.oct_eye);
        MAP.put("tag", R.drawable.oct_tag);
        MAP.put("pulse", R.drawable.oct_pulse);
        MAP.put("comment", R.drawable.oct_comment);
    }

    private Icons() {
    }

    public static int drawable(String name) {
        Integer id = name == null ? null : MAP.get(name);
        return id == null ? R.drawable.oct_globe : id;
    }
}
