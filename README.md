<div align="center">

# هم‌زبان · Hamzaban

**مترجم فارسی ⇄ انگلیسی که همه‌چیزش از اول داخل برنامه است.**

بدون دانلود · بدون حساب کاربری · بدون شبکه · ۵٫۵ مگابایت

ساخته‌شده توسط امیرمهدی (AM Studio)

</div>

---

## فارسی

هم‌زبان یک مترجم دوسویهٔ فارسی ⇄ انگلیسی برای اندروید است. تمام داده — حدود ۱۰۸ هزار مدخل، شامل عبارت‌های کامل و واژه‌نامهٔ دوزبانه — داخل خودِ برنامه بسته‌بندی شده است. اولین اجرا هیچ دانلودی ندارد، برنامه در یک ثانیه باز می‌شود و در حالت هواپیما هم کار می‌کند.

### ویژگی‌ها

- **صفر دانلود**: داده از اول داخل APK است؛ حجم کل برنامه حدود ۵٫۵ مگابایت.
- **کاملاً آفلاین**: هیچ درخواست شبکه‌ای وجود ندارد — مجوز `INTERNET` هم درخواست نمی‌شود.
- **دوسویه**: فارسی ← انگلیسی و انگلیسی ← فارسی با یک ضربه.
- **دو حالت ترجمه** که صادقانه برچسب می‌خورند: «عبارت دقیق» برای تطابق کامل با عبارت‌های موجود، و «واژه‌به‌واژه» برای متن آزاد.
- **تاریخچه و ذخیره‌ها** روی خود دستگاه (SQLite محلی)، با جست‌وجو، کشیدن برای حذف و نشانک.
- **خواندن با صدا** (TTS دستگاه)، کپی و اشتراک‌گذاری.
- **راست‌چین درست**: فونت وزیرمتن، اعداد فارسی، چیدمان منطقی و دوسویه (bidi).
- **تم روشن/تیره/سیستم**، اندازهٔ متن قابل تنظیم، رابط فارسی و انگلیسی.
- **طراحی تیره‌محور** با لهجهٔ بنفش، تایپوگرافی سیاه و فضای سفید زیاد.

### صادقانه دربارهٔ موتور

این برنامه یک موتور ترجمهٔ عصبی (NMT) نیست. موتورهای NMT نیاز به دانلود مدل چند ده مگابایتی دارند و دقیقاً همان چیزی هستند که شما نخواستید. به‌جای آن، دادهٔ موازی واقعی (پیکرهٔ OPUS فارسی–انگلیسی، شامل جمله‌های Tatoeba و دیگر پیکره‌های آزاد) فشرده و بسته‌بندی شده است:

1. اگر متن ورودی دقیقاً با یکی از عبارت‌های پیکره یکی باشد، ترجمهٔ انسانیِ همان عبارت برگردانده می‌شود («عبارت دقیق»).
2. در غیر این صورت، واژه‌به‌واژه با واژه‌نامهٔ دوزبانه ترجمه می‌شود، با نرمال‌سازی فارسی (ی/ک عربی، اعراب، نیم‌فاصله، ارقام) و یک قاعدهٔ ساده برای فعل ربطی («است/هستم/...» → is/am/are).

نتیجه برای واژه‌ها و جمله‌های کوتاه و رایج خوب است و برای جمله‌های بلند و آزاد تقریبی. برنامه هم این را پنهان نمی‌کند و حالت هر ترجمه را نشان می‌دهد.

### نصب

از بخش [Releases](https://github.com/amirmahdipa77-tech/hamzaban/releases) فایل APK را دانلود کنید.

- حداقل اندروید **۸.۰ (API 26)**
- معماری‌ها: `arm64-v8a` و `armeabi-v7a`

### ساخت از سورس

```bash
git clone https://github.com/amirmahdipa77-tech/hamzaban.git
cd hamzaban
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
chmod +x gradlew
./gradlew :app:assembleRelease
```

دادهٔ واژه‌نامه از قبل در `app/src/main/assets/lexicon.binz` هست؛ برای ساخت APK به هیچ دانلودی نیاز نیست.

برای امضای نسخهٔ release یک فایل `keystore.properties` در ریشهٔ پروژه بسازید (در `.gitignore` است) با کلیدهای `storeFile`، `storePassword`، `keyAlias` و `keyPassword`. بدون آن، APK بدون امضا ساخته می‌شود.

### معماری

- **Java 17** + **AndroidX** (AppCompat، بدون Compose و بدون Material Components)
- **XML layout + ViewBinding**، MVVM سبک با `ViewModel` + `LiveData`
- **SQLite** خام برای تاریخچه، بدون annotation processor
- واژه‌نامه در یک فایل باینری مرتب‌شده بر اساس هش FNV-1a، با ایندکس تُنُک؛ در زمان اجرا با `MappedByteBuffer` خوانده می‌شود (بدون بارگذاری کامل در حافظه)
- طراحی و آیکون‌ها از GitHub الگو گرفته شده‌اند: توکن‌های Primer و Octicons

```
app/src/main/java/studio/am/hamzaban/
├── App.java · BaseActivity.java · SplashActivity.java
├── OnboardingActivity.java · MainActivity.java
├── core/       Lexicon (خواندن mmap) · Translator (نرمال‌سازی + موتور)
├── data/       Prefs · DbHelper · HistoryRepo · Entry
├── ui/         TranslateFragment · EntryListFragment · SettingsFragment
├── ui/widget/  IconView · RingView · EntryAdapter
├── util/       Fa (اعداد فارسی) · LocaleHelper · Tts · Ui · TimeFmt
└── vm/         TranslateViewModel · HistoryViewModel
```

### حریم خصوصی

هیچ مجوزی جز هیچ. برنامه `INTERNET` هم درخواست نمی‌کند. متن، تاریخچه و تنظیمات هرگز از دستگاه خارج نمی‌شوند.

### مجوز

MIT — فایل [LICENSE](LICENSE).

---

## English

Hamzaban is a bidirectional Persian ⇄ English translator for Android. The whole dataset — roughly 108k entries of parallel phrases and a bilingual lexicon — ships inside the app. First launch downloads nothing, the app opens in about a second, and it works in airplane mode.

### Features

- **Zero downloads**: the data is in the APK; the whole app is about 5.5 MB.
- **Truly offline**: no network calls at all — the `INTERNET` permission isn't even requested.
- **Bidirectional** Persian → English and English → Persian in one tap.
- **Two honestly labelled modes**: "Exact phrase" for a full match in the corpus, "Word by word" for free text.
- **History and bookmarks** stored locally in SQLite, with search and swipe-to-delete.
- **Text-to-speech**, copy and share.
- **Proper RTL**: Vazirmatn, Persian digits, logical and bidi-correct layout.
- **Light / dark / system theme**, adjustable text size, Persian and English UI.

### Honest note on the engine

This is not a neural machine translation engine. NMT needs a model download of tens of megabytes — exactly what you asked to avoid. Instead the real parallel data (the OPUS Persian–English corpus, including Tatoeba and other open corpora) is compressed and bundled:

1. An exact match in the corpus returns the human translation ("Exact phrase").
2. Otherwise the text is translated word by word from the bilingual lexicon, with Persian normalisation (Arabic yeh/kaf, diacritics, ZWNJ, digits) and a simple copula rule.

That is strong for words and short common sentences and approximate for long free-form text. The app shows which mode produced each result rather than pretending otherwise.

### Install

Download the APK from [Releases](https://github.com/amirmahdipa77-tech/hamzaban/releases). Android 8.0 (API 26)+, `arm64-v8a` and `armeabi-v7a`.

### Build from source

```bash
git clone https://github.com/amirmahdipa77-tech/hamzaban.git
cd hamzaban
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
chmod +x gradlew
./gradlew :app:assembleRelease
```

The lexicon is already committed at `app/src/main/assets/lexicon.binz`, so the build needs no downloads. For a signed release add a git-ignored `keystore.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`); without it the APK is unsigned.

### Architecture

Java 17 + AndroidX (AppCompat, no Compose, no Material Components), XML layouts with ViewBinding, lightweight MVVM, plain SQLite, and a hash-sorted binary lexicon read through a `MappedByteBuffer` with a sparse index — no full load into memory. Icons are GitHub's Octicons, converted to vector drawables, and the whole visual system follows GitHub's Primer design tokens.

### Privacy

No permissions at all — the app doesn't even request `INTERNET`. Text, history and settings never leave the device.

### License

MIT — see [LICENSE](LICENSE).

---

<div align="center">

Made by **Amirmahdi** · **AM Studio**

</div>
