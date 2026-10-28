package studio.am.hamzaban;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import studio.am.hamzaban.data.Prefs;

public class App extends Application {

    private static App instance;
    private Prefs prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = new Prefs(this);
        if (!prefs.hasLang()) {
            String system = java.util.Locale.getDefault().getLanguage();
            prefs.setLang("en".equals(system) ? "en" : "fa");
        }
        applyTheme();
    }

    public void applyTheme() {
        int mode = prefs.theme();
        int nightMode = mode == 1 ? AppCompatDelegate.MODE_NIGHT_NO
                : mode == 2 ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    public static App get() {
        return instance;
    }

    public static Prefs prefs() {
        if (instance == null) return null;
        return instance.prefs;
    }
}
