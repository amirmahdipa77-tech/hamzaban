<div align="center">

# هم‌زبان · Hamzaban

**مترجم هم‌زمان فارسی ⇄ انگلیسی، با موتور عصبی روی دستگاه.**

تایپ کن، همان لحظه ترجمه می‌شود · بدون حساب کاربری · بدون سرور

ساخته‌شده توسط امیرمهدی (AM Studio)

</div>

---

## فارسی

هم‌زبان یک مترجم دوطرفهٔ فارسی ⇄ انگلیسی است که **هم‌زمان با تایپ** ترجمه می‌کند. دو موتور دارد که با هم کار می‌کنند:

1. **واژه‌نامهٔ داخلی** — حدود ۱۰۸ هزار مدخل که از اول داخل برنامه است؛ از اولین ثانیه کار می‌کند، حتی بدون اینترنت.
2. **موتور عصبی (NMT)** — مدل ترجمهٔ روی‌دستگاه؛ اولین بار که یک زبان انتخاب می‌شود، مدلش یک‌بار دانلود می‌شود و بعد از آن همه‌چیز آفلاین است.

نتیجه: با باز کردن برنامه و بدون هیچ دانلودی می‌توانی ترجمه بگیری، و به‌محض آماده شدن مدل، جمله‌های واقعی با کیفیت عصبی ترجمه می‌شوند.

### ویژگی‌ها

- **ترجمهٔ هم‌زمان (real-time)**: با هر تغییر متن، بعد از ۳۲۰ میلی‌ثانیه ترجمه به‌روز می‌شود.
- **دو موتوره**: واژه‌نامهٔ فوری + مدل عصبی قدرتمند؛ برچسب کنار هر ترجمه می‌گوید از کدام آمده («عصبی» / «عبارت دقیق» / «واژه‌به‌واژه»).
- **دوسویه** فارسی ← انگلیسی و انگلیسی ← فارسی با یک ضربه.
- **خواندن با صدا**، کپی و اشتراک‌گذاری.
- **راست‌چین درست**: فونت وزیرمتن، اعداد فارسی، چیدمان منطقی و دوسویه.
- **تم روشن/تیره/سیستم** و اندازهٔ متن قابل تنظیم در صفحهٔ تنظیمات.
- **طراحی بر اساس GitHub و Reddit**: توکن‌های Primer، حاشیهٔ ۱ پیکسلی، آیکون‌های Octicons، چیدمان فشرده و ردیفی.

### حریم خصوصی

هیچ داده‌ای جمع‌آوری نمی‌شود و هیچ سروری در کار نیست. مجوزهای `INTERNET` و `ACCESS_NETWORK_STATE` فقط برای دانلود یک‌بارهٔ مدل عصبی هستند؛ متن شما جایی فرستاده نمی‌شود و ترجمه کاملاً روی دستگاه انجام می‌شود.

### نصب

از بخش [Releases](https://github.com/amirmahdipa77-tech/hamzaban/releases) فایل APK را دانلود کنید.

- حداقل اندروید **۸.۰ (API 26)**
- معماری‌ها: `arm64-v8a` و `armeabi-v7a`
- حجم: حدود ۳۴ مگابایت (کتابخانهٔ موتور عصبی روی‌دستگاه)

### ساخت از سورس

```bash
git clone https://github.com/amirmahdipa77-tech/hamzaban.git
cd hamzaban
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
chmod +x gradlew
./gradlew :app:assembleRelease
```

برای امضای نسخهٔ release یک فایل `keystore.properties` در ریشهٔ پروژه بسازید (در `.gitignore` است) با کلیدهای `storeFile`، `storePassword`، `keyAlias` و `keyPassword`.

### معماری

- **Java 17** + **AndroidX** (AppCompat، بدون Compose و بدون Material Components)
- **XML layout + ViewBinding**، MVVM سبک با `ViewModel` + `LiveData`
- `core/NmtEngine` — موتور عصبی ML Kit با کش و دانلود تنبل مدل
- `core/Lexicon` — واژه‌نامهٔ باینری مرتب‌شده بر اساس هش FNV-1a، خوانده‌شده با `MappedByteBuffer` و ایندکس تُنُک
- `core/Translator` — نرمال‌سازی فارسی (ی/ک عربی، اعراب، نیم‌فاصله، ارقام) و تطابق دقیق/واژه‌به‌واژه
- آیکون‌ها: **Octicons** گیت‌هاب، تبدیل‌شده به وکتور اندروید

```
app/src/main/java/studio/am/hamzaban/
├── App.java · BaseActivity.java · SplashActivity.java
├── OnboardingActivity.java · MainActivity.java · SettingsActivity.java
├── core/       NmtEngine · Lexicon · Translator
├── data/       Prefs
├── ui/widget/  IconView · Icons
├── util/       Fa · LocaleHelper · Tts · Ui
└── vm/         TranslateViewModel
```

### مجوز

MIT — فایل [LICENSE](LICENSE).

---

## English

Hamzaban is a bidirectional Persian ⇄ English translator that translates **as you type**. It runs two engines together:

1. **Bundled lexicon** — ~108k entries shipped inside the app, available from the first second with no network at all.
2. **Neural engine (NMT)** — an on-device translation model. The first time a language is used its model is downloaded once; after that everything is offline.

### Features

- **Real-time translation**: every change is translated after a 320 ms debounce.
- **Two engines**: instant lexicon plus a real neural model, with a label on each result telling you which one produced it.
- **Bidirectional** Persian → English and English → Persian in one tap.
- **Text-to-speech**, copy and share.
- **Proper RTL**: Vazirmatn, Persian digits, logical and bidi-correct layout.
- **Light / dark / system theme** and adjustable text size on a dedicated settings screen.
- **GitHub/Reddit-inspired UI**: Primer tokens, 1px borders, Octicons, compact row-based layout.

### Privacy

Nothing is collected and there is no backend. `INTERNET` and `ACCESS_NETWORK_STATE` are used only for the one-time model download; your text never leaves the device and translation runs locally.

### Install

Download the APK from [Releases](https://github.com/amirmahdipa77-tech/hamzaban/releases). Android 8.0 (API 26)+, `arm64-v8a` and `armeabi-v7a`, ~34 MB.

### Build from source

```bash
git clone https://github.com/amirmahdipa77-tech/hamzaban.git
cd hamzaban
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
chmod +x gradlew
./gradlew :app:assembleRelease
```

For a signed release add a git-ignored `keystore.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`).

### Architecture

Java 17 + AndroidX (AppCompat, no Compose, no Material Components), XML layouts with ViewBinding, lightweight MVVM. `NmtEngine` wraps ML Kit's on-device translation with a cached, lazily downloaded model; `Lexicon` is a hash-sorted binary read through a `MappedByteBuffer` with a sparse index; `Translator` handles Persian normalisation and exact/word-by-word matching. Icons are GitHub's Octicons as vector drawables.

### License

MIT — see [LICENSE](LICENSE).

---

<div align="center">

Made by **Amirmahdi** · **AM Studio**

</div>
