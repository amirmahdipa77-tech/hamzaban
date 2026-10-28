package studio.am.hamzaban.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import studio.am.hamzaban.util.LocaleHelper;

/**
 * The 59 languages supported by the on-device translation models.
 */
public final class Langs {

    public static final List<Lang> ALL;

    static {
        List<Lang> l = new ArrayList<>();
        l.add(new Lang("af", "Afrikaans", "آفریکانس", "Afrikaans"));
        l.add(new Lang("sq", "Shqip", "آلبانیایی", "Albanian"));
        l.add(new Lang("ar", "العربية", "عربی", "Arabic"));
        l.add(new Lang("be", "Беларуская", "بلاروسی", "Belarusian"));
        l.add(new Lang("bn", "বাংলা", "بنگالی", "Bengali"));
        l.add(new Lang("bg", "Български", "بلغاری", "Bulgarian"));
        l.add(new Lang("ca", "Català", "کاتالان", "Catalan"));
        l.add(new Lang("zh", "中文", "چینی", "Chinese"));
        l.add(new Lang("hr", "Hrvatski", "کروات", "Croatian"));
        l.add(new Lang("cs", "Čeština", "چکی", "Czech"));
        l.add(new Lang("da", "Dansk", "دانمارکی", "Danish"));
        l.add(new Lang("nl", "Nederlands", "هلندی", "Dutch"));
        l.add(new Lang("en", "English", "انگلیسی", "English"));
        l.add(new Lang("eo", "Esperanto", "اسپرانتو", "Esperanto"));
        l.add(new Lang("et", "Eesti", "استونیایی", "Estonian"));
        l.add(new Lang("fi", "Suomi", "فنلاندی", "Finnish"));
        l.add(new Lang("fr", "Français", "فرانسوی", "French"));
        l.add(new Lang("gl", "Galego", "گالیسیایی", "Galician"));
        l.add(new Lang("ka", "ქართული", "گرجی", "Georgian"));
        l.add(new Lang("de", "Deutsch", "آلمانی", "German"));
        l.add(new Lang("el", "Ελληνικά", "یونانی", "Greek"));
        l.add(new Lang("gu", "ગુજરાતી", "گجراتی", "Gujarati"));
        l.add(new Lang("ht", "Kreyòl ayisyen", "کریول هائیتی", "Haitian Creole"));
        l.add(new Lang("he", "עברית", "عبری", "Hebrew"));
        l.add(new Lang("hi", "हिन्दी", "هندی", "Hindi"));
        l.add(new Lang("hu", "Magyar", "مجاری", "Hungarian"));
        l.add(new Lang("is", "Íslenska", "ایسلندی", "Icelandic"));
        l.add(new Lang("id", "Bahasa Indonesia", "اندونزیایی", "Indonesian"));
        l.add(new Lang("ga", "Gaeilge", "ایرلندی", "Irish"));
        l.add(new Lang("it", "Italiano", "ایتالیایی", "Italian"));
        l.add(new Lang("ja", "日本語", "ژاپنی", "Japanese"));
        l.add(new Lang("kn", "ಕನ್ನಡ", "کانادا", "Kannada"));
        l.add(new Lang("kk", "Қазақша", "قزاقی", "Kazakh"));
        l.add(new Lang("ko", "한국어", "کره‌ای", "Korean"));
        l.add(new Lang("lv", "Latviešu", "لتونیایی", "Latvian"));
        l.add(new Lang("lt", "Lietuvių", "لیتوانیایی", "Lithuanian"));
        l.add(new Lang("mk", "Македонски", "مقدونی", "Macedonian"));
        l.add(new Lang("ms", "Bahasa Melayu", "مالایی", "Malay"));
        l.add(new Lang("mr", "मराठी", "مراتی", "Marathi"));
        l.add(new Lang("mn", "Монгол", "مغولی", "Mongolian"));
        l.add(new Lang("no", "Norsk", "نروژی", "Norwegian"));
        l.add(new Lang("fa", "فارسی", "فارسی", "Persian"));
        l.add(new Lang("pl", "Polski", "لهستانی", "Polish"));
        l.add(new Lang("pt", "Português", "پرتغالی", "Portuguese"));
        l.add(new Lang("ro", "Română", "رومانیایی", "Romanian"));
        l.add(new Lang("ru", "Русский", "روسی", "Russian"));
        l.add(new Lang("sk", "Slovenčina", "اسلواکی", "Slovak"));
        l.add(new Lang("sl", "Slovenščina", "اسلوونیایی", "Slovenian"));
        l.add(new Lang("es", "Español", "اسپانیایی", "Spanish"));
        l.add(new Lang("sw", "Kiswahili", "سواحیلی", "Swahili"));
        l.add(new Lang("sv", "Svenska", "سوئدی", "Swedish"));
        l.add(new Lang("tl", "Tagalog", "تاگالوگ", "Tagalog"));
        l.add(new Lang("ta", "தமிழ்", "تامیلی", "Tamil"));
        l.add(new Lang("te", "తెలుగు", "تلوگو", "Telugu"));
        l.add(new Lang("th", "ไทย", "تایلندی", "Thai"));
        l.add(new Lang("tr", "Türkçe", "ترکی", "Turkish"));
        l.add(new Lang("uk", "Українська", "اوکراینی", "Ukrainian"));
        l.add(new Lang("ur", "اردو", "اردو", "Urdu"));
        l.add(new Lang("vi", "Tiếng Việt", "ویتنامی", "Vietnamese"));
        l.add(new Lang("cy", "Cymraeg", "ولزی", "Welsh"));
        ALL = Collections.unmodifiableList(l);
    }

    private Langs() {
    }

    public static Lang byCode(String code) {
        if (code != null) {
            for (Lang lang : ALL) {
                if (lang.code.equals(code)) return lang;
            }
        }
        return new Lang(code == null ? "en" : code, code == null ? "English" : code, code == null ? "English" : code, code == null ? "English" : code);
    }

    public static String displayName(Context c, String code) {
        return byCode(code).name(LocaleHelper.isFa(c));
    }

    public static List<Lang> search(String q) {
        if (q == null || q.trim().isEmpty()) return ALL;
        String needle = q.trim().toLowerCase(java.util.Locale.US);
        List<Lang> out = new ArrayList<>();
        for (Lang lang : ALL) {
            if (lang.fa.contains(needle)
                    || lang.en.toLowerCase(java.util.Locale.US).contains(needle)
                    || lang.nativeName.toLowerCase(java.util.Locale.US).contains(needle)
                    || lang.code.contains(needle)) {
                out.add(lang);
            }
        }
        return out;
    }
}
