package studio.am.hamzaban.util;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

import studio.am.hamzaban.data.Prefs;

public final class LocaleHelper {

    private LocaleHelper() {
    }

    public static Context wrap(Context base) {
        try {
            Prefs prefs = new Prefs(base);
            Locale locale = Locale.forLanguageTag(prefs.lang());
            Locale.setDefault(locale);
            Configuration cfg = new Configuration(base.getResources().getConfiguration());
            cfg.setLocale(locale);
            cfg.setLayoutDirection(locale);
            float baseScale = base.getResources().getConfiguration().fontScale;
            cfg.fontScale = baseScale * prefs.fontScale();
            return base.createConfigurationContext(cfg);
        } catch (Throwable t) {
            return base;
        }
    }

    public static boolean isFa(Context c) {
        try {
            return "fa".equals(c.getResources().getConfiguration().getLocales().get(0).getLanguage());
        } catch (Throwable t) {
            return true;
        }
    }
}
