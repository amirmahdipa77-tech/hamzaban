package studio.am.hamzaban.core;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Bundled bidirectional Persian / English translation engine. */
public final class Translator {

    public static final int MODE_NONE = 0;
    public static final int MODE_PHRASE = 1;
    public static final int MODE_WORDS = 2;

    public static final class Result {
        public final String text;
        public final int mode;
        public final int words;

        Result(String text, int mode, int words) {
            this.text = text;
            this.mode = mode;
            this.words = words;
        }
    }

    private static final Pattern SPACES = Pattern.compile("\\s+");
    private static final Pattern DIACRITICS = Pattern.compile("[\u064B-\u0652\u0670\u0640]");
    private static final Pattern ARABIC = Pattern.compile("[\u0600-\u06FF]");
    private static final String EDGE_CHARS = "\\s.,!?;:\"'\u2026\u2019\u201c\u201d";
    private static final Pattern EDGE_EN = Pattern.compile("[" + EDGE_CHARS + "]+");
    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^\\p{L}\\p{N}']+");

    private static final Set<String> COPULAS = new HashSet<>(Arrays.asList(
            "\u0627\u0633\u062a", "\u0647\u0633\u062a", "\u0647\u0633\u062a\u0646\u062f",
            "\u0647\u0633\u062a\u0645", "\u0647\u0633\u062a\u06cc", "\u0647\u0633\u062a\u06cc\u0645",
            "\u0647\u0633\u062a\u06cc\u062f", "\u0628\u0648\u062f", "\u0628\u0648\u062f\u0645",
            "\u0628\u0648\u062f\u06cc", "\u0628\u0648\u062f\u0646\u062f"));

    private static final String[][] COPULA_MAP = {
            {"\u0647\u0633\u062a\u0645", "am"},
            {"\u0647\u0633\u062a\u06cc", "are"},
            {"\u0647\u0633\u062a\u06cc\u0645", "are"},
            {"\u0647\u0633\u062a\u06cc\u062f", "are"},
            {"\u0647\u0633\u062a\u0646\u062f", "are"},
            {"\u0647\u0633\u062a", "is"},
            {"\u0627\u0633\u062a", "is"},
            {"\u0628\u0648\u062f\u0645", "was"},
            {"\u0628\u0648\u062f\u06cc", "was"},
            {"\u0628\u0648\u062f\u0646\u062f", "were"},
            {"\u0628\u0648\u062f", "was"},
    };

    private Translator() {
    }

    public static boolean isPersian(String text) {
        return text != null && ARABIC.matcher(text).find();
    }

    public static Result translate(String input, boolean toEnglish) {
        if (input == null) return new Result("", MODE_NONE, 0);
        String trimmed = SPACES.matcher(input).replaceAll(" ").trim();
        if (trimmed.isEmpty()) return new Result("", MODE_NONE, 0);

        Lexicon lex = Lexicon.ready();
        if (lex == null) return new Result("", MODE_NONE, 0);
        String key = toEnglish ? normalizeFa(trimmed) : normalizeEn(trimmed);
        String hit = lex.lookup(key);
        if (hit == null) {
            String loose = toEnglish ? stripFa(key) : stripEn(key);
            if (!loose.equals(key) && !loose.isEmpty()) {
                hit = lex.lookup(loose);
            }
        }
        if (hit != null && !hit.isEmpty()) {
            return new Result(hit, MODE_PHRASE, 0);
        }
        return wordByWord(trimmed, toEnglish);
    }

    private static Result wordByWord(String text, boolean toEnglish) {
        Lexicon lex = Lexicon.ready();
        if (lex == null) return new Result("", MODE_NONE, 0);
        List<String> tokens = new ArrayList<>();
        Matcher m = TOKEN_SPLIT.matcher(text);
        int last = 0;
        while (m.find()) {
            if (m.start() > last) tokens.add(text.substring(last, m.start()));
            tokens.add(m.group());
            last = m.end();
        }
        if (last < text.length()) tokens.add(text.substring(last));

        List<String> words = new ArrayList<>();
        for (String t : tokens) {
            if (!t.trim().isEmpty()) words.add(t);
        }
        if (words.isEmpty()) return new Result("", MODE_NONE, 0);

        boolean copula = false;
        String copulaWord = null;
        if (toEnglish && words.size() > 1) {
            String tail = normalizeFa(words.get(words.size() - 1));
            if (COPULAS.contains(tail)) {
                copula = true;
                copulaWord = copulaFor(tail);
                words = new ArrayList<>(words.subList(0, words.size() - 1));
            }
        }

        List<String> out = new ArrayList<>();
        int translated = 0;
        for (String w : words) {
            String nk = toEnglish ? normalizeFa(w) : normalizeEn(w);
            String v = nk.isEmpty() ? null : lex.lookup(nk);
            if (v == null && nk.length() > 4 && nk.endsWith("\u0647\u0627")) {
                String base = lex.lookup(nk.substring(0, nk.length() - 2));
                if (base != null && !base.isEmpty()) v = base + "s";
            }
            if (v != null && !v.isEmpty()) {
                out.add(v);
                translated++;
            } else {
                out.add(w);
            }
        }
        if (translated == 0) {
            return new Result("", MODE_NONE, 0);
        }

        StringBuilder sb = new StringBuilder();
        int insertAt = copula ? Math.min(1, out.size()) : -1;
        for (int i = 0; i < out.size(); i++) {
            if (i == insertAt) sb.append(copulaWord).append(' ');
            sb.append(out.get(i));
            if (i < out.size() - 1) sb.append(' ');
        }
        if (insertAt >= out.size() && copulaWord != null) {
            sb.append(' ').append(copulaWord);
        }
        String result = SPACES.matcher(sb.toString()).replaceAll(" ").trim();
        if (toEnglish && !result.isEmpty()) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }
        return new Result(result, MODE_WORDS, translated);
    }

    private static String copulaFor(String token) {
        for (String[] pair : COPULA_MAP) {
            if (pair[0].equals(token)) return pair[1];
        }
        return "is";
    }

    public static String normalizeFa(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFC);
        StringBuilder b = new StringBuilder(n.length());
        for (int i = 0; i < n.length(); i++) {
            char ch = n.charAt(i);
            switch (ch) {
                case '\u064a':
                case '\u0649':
                case '\u0626':
                    b.append('\u06cc');
                    break;
                case '\u0643':
                    b.append('\u06a9');
                    break;
                case '\u0629':
                case '\u06c0':
                case '\u06c1':
                    b.append('\u0647');
                    break;
                case '\u0622':
                case '\u0623':
                case '\u0625':
                    b.append('\u0627');
                    break;
                case '\u0624':
                    b.append('\u0648');
                    break;
                case '\u200c':
                    b.append(' ');
                    break;
                default:
                    b.append(ch);
            }
        }
        String out = DIACRITICS.matcher(b.toString()).replaceAll("");
        for (int i = 0; i < 10; i++) {
            out = out.replace((char) ('\u06f0' + i), (char) ('0' + i));
        }
        return SPACES.matcher(out).replaceAll(" ").trim();
    }

    public static String normalizeEn(String s) {
        if (s == null) return "";
        String out = s.toLowerCase(Locale.US).replace('\u2019', '\'');
        out = SPACES.matcher(out).replaceAll(" ").trim();
        return EDGE_EN.matcher(out).replaceAll("");
    }

    private static String stripFa(String s) {
        String out = s;
        int start = 0;
        int end = out.length();
        while (start < end && isFaEdge(out.charAt(start))) start++;
        while (end > start && isFaEdge(out.charAt(end - 1))) end--;
        return out.substring(start, end);
    }

    private static boolean isFaEdge(char c) {
        return c == ' ' || c == '\u060c' || c == '\u061f' || c == '.' || c == '!'
                || c == '?' || c == ':' || c == ';' || c == '-';
    }

    private static String stripEn(String s) {
        return EDGE_EN.matcher(s).replaceAll("");
    }
}
