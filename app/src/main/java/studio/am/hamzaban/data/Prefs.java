package studio.am.hamzaban.data;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    private static final String FILE = "hamzaban_prefs";

    private final SharedPreferences sp;

    public Prefs(Context c) {
        Context app = c == null ? null : c.getApplicationContext();
        if (app == null) app = c;
        sp = app.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public String src() {
        return sp.getString("src", "fa");
    }

    public void setSrc(String v) {
        sp.edit().putString("src", v).apply();
    }

    public String tgt() {
        return sp.getString("tgt", "en");
    }

    public void setTgt(String v) {
        sp.edit().putString("tgt", v).apply();
    }

    public int theme() {
        return sp.getInt("theme", 0);
    }

    public void setTheme(int v) {
        sp.edit().putInt("theme", v).apply();
    }

    public String lang() {
        return sp.getString("lang", "fa");
    }

    public boolean hasLang() {
        return sp.contains("lang");
    }

    public void setLang(String v) {
        sp.edit().putString("lang", v).apply();
    }

    public int fontIndex() {
        return sp.getInt("font", 1);
    }

    public void setFontIndex(int v) {
        sp.edit().putInt("font", v).apply();
    }

    public float fontScale() {
        int i = fontIndex();
        if (i == 0) return 0.9f;
        if (i == 2) return 1.14f;
        return 1f;
    }

    public boolean onboarded() {
        return sp.getBoolean("onboarded", false);
    }

    public void setOnboarded(boolean v) {
        sp.edit().putBoolean("onboarded", v).apply();
    }

    public boolean setupDone() {
        return sp.getBoolean("setup", false);
    }

    public void setSetupDone(boolean v) {
        sp.edit().putBoolean("setup", v).apply();
    }
}
