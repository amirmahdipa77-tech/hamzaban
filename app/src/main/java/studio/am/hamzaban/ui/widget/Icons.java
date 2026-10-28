package studio.am.hamzaban.ui.widget;

import java.util.HashMap;
import java.util.Map;

/** Material Symbols Rounded glyph map (subset). */
public final class Icons {

    private static final Map<String, Integer> MAP = new HashMap<>();

    static {
        MAP.put("translate", 0xe8e2);
        MAP.put("swap_horiz", 0xe8d4);
        MAP.put("swap_vert", 0xe8d5);
        MAP.put("content_copy", 0xe14d);
        MAP.put("volume_up", 0xe050);
        MAP.put("share", 0xe80d);
        MAP.put("bookmark", 0xe8e7);
        MAP.put("history", 0xe8b3);
        MAP.put("settings", 0xe8b8);
        MAP.put("delete", 0xe92e);
        MAP.put("close", 0xe5cd);
        MAP.put("check", 0xe668);
        MAP.put("search", 0xef7a);
        MAP.put("arrow_back", 0xe5c4);
        MAP.put("expand_more", 0xe5cf);
        MAP.put("download", 0xf090);
        MAP.put("download_done", 0xf091);
        MAP.put("star", 0xf09a);
        MAP.put("chevron_right", 0xe5cc);
        MAP.put("chevron_left", 0xe5cb);
        MAP.put("refresh", 0xe5d5);
        MAP.put("more_vert", 0xe5d4);
        MAP.put("add", 0xe145);
        MAP.put("dark_mode", 0xe51c);
        MAP.put("light_mode", 0xe518);
        MAP.put("language", 0xea07);
        MAP.put("tune", 0xe429);
        MAP.put("info", 0xe88e);
        MAP.put("offline_bolt", 0xe932);
        MAP.put("cloud_off", 0xe2c1);
        MAP.put("autorenew", 0xe863);
        MAP.put("auto_awesome", 0xe65f);
        MAP.put("check_circle", 0xf0be);
        MAP.put("wifi_off", 0xe648);
        MAP.put("done_all", 0xe877);
        MAP.put("edit", 0xf097);
        MAP.put("stop", 0xe047);
        MAP.put("record_voice_over", 0xe91f);
        MAP.put("arrow_forward", 0xe5c8);
    }

    private Icons() {
    }

    public static int codepoint(String name) {
        Integer cp = name == null ? null : MAP.get(name);
        return cp == null ? 0xe8e2 : cp;
    }
}
