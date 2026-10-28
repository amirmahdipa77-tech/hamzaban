<div align="center">

# هم‌زبان · Hamzaban

**مترجم آفلاین، بدون اینترنت، بدون حساب کاربری.**

۵۹ زبان · موتور ترجمهٔ عصبی روی دستگاه · ساخته‌شده توسط امیرمهدی (AM Studio)

</div>

---

## فارسی

هم‌زبان یک مترجم کاملاً آفلاین برای اندروید است. بعد از یک دانلود یک‌بارهٔ مدل زبانی، ترجمه کاملاً روی گوشی شما انجام می‌شود: هیچ متنی به هیچ سروری فرستاده نمی‌شود، هیچ حساب کاربری لازم نیست و هیچ داده‌ای جمع‌آوری نمی‌شود.

### ویژگی‌ها

- **۵۹ زبان** با موتور ترجمهٔ عصبی (NMT) که روی دستگاه اجرا می‌شود.
- **دانلود یک‌باره، استفادهٔ همیشگی**: در اولین اجرا فقط زبان‌های انتخابی شما دانلود می‌شوند؛ بقیهٔ زبان‌ها هر وقت خواستید اضافه یا پاک می‌شوند.
- **کاملاً آفلاین** بعد از دانلود مدل.
- **تاریخچه و ذخیره‌ها** روی همان دستگاه (SQLite محلی)، با جست‌وجو، کشیدن برای حذف و نشانک.
- **خواندن با صدا** (TTS دستگاه)، کپی، و اشتراک‌گذاری ترجمه.
- **جابه‌جایی سریع زبان‌ها** با یک ضربه؛ متن ترجمه‌شده به‌طور خودکار به سمت مقابل منتقل می‌شود.
- **راست‌چین و فارسی‌محور** با فونت وزیرمتن، اعداد فارسی و چیدمان منطقی (RTL) درست.
- **تم روشن/تیره/سیستم**، اندازهٔ متن قابل تنظیم، و رابط کاربری فارسی و انگلیسی.
- **سبک و بی‌سر‌و‌صدا**: بدون تبلیغ، بدون ردیابی، بدون صفحهٔ اضافه.

### حریم خصوصی

هم‌زبان هیچ داده‌ای جمع‌آوری نمی‌کند. تنها دسترسی‌های اعلام‌شده `INTERNET` و `ACCESS_NETWORK_STATE` هستند و فقط برای دانلود یک‌بارهٔ مدل‌های زبانی استفاده می‌شوند. متن شما، تاریخچه و تنظیمات هرگز از دستگاه خارج نمی‌شوند.

### نصب

آخرین نسخه را از بخش [Releases](https://github.com/amirmahdipa77-tech/hamzaban/releases) دانلود و نصب کنید.

- حداقل اندروید: **۸.۰ (API 26)**
- معماری‌ها: `arm64-v8a` و `armeabi-v7a`

### ساخت از سورس

```bash
git clone https://github.com/amirmahdipa77-tech/hamzaban.git
cd hamzaban
```

یک فایل `local.properties` در ریشهٔ پروژه بسازید:

```properties
sdk.dir=/path/to/Android/Sdk
```

و سپس:

```bash
./gradlew :app:assembleRelease
```

برای امضای نسخهٔ release یک فایل `keystore.properties` در ریشهٔ پروژه بسازید (این فایل در `.gitignore` است):

```properties
storeFile=../keys/release.jks
storePassword=******
keyAlias=******
keyPassword=******
```

اگر این فایل نباشد، APK بدون امضا ساخته می‌شود.

### معماری

- **Java 17** + **AndroidX** (AppCompat / Material Components، بدون Jetpack Compose)
- **XML layout + ViewBinding**
- **MVVM** سبک با `ViewModel` + `LiveData`
- **SQLite** خام برای تاریخچه (بدون annotation processor)
- موتور ترجمه: **ML Kit on-device Translation** (مدل‌ها روی دستگاه اجرا می‌شوند)
- بدون تبلیغ، بدون SDK تحلیلی، بدون شبکه در زمان ترجمه

```
app/src/main/java/studio/am/hamzaban/
├── App.java, BaseActivity.java, SplashActivity.java
├── OnboardingActivity.java, SetupActivity.java, MainActivity.java
├── core/       TranslatorEngine — کش موتورهای ترجمه
├── data/       Prefs · Langs · DbHelper · HistoryRepo
├── ui/         TranslateFragment · EntryListFragment · SettingsFragment · LanguagePickerSheet
├── ui/widget/  IconView (Material Symbols Rounded) · RingView · EntryAdapter
├── util/       Fa (اعداد فارسی) · LocaleHelper · Tts · Ui
└── vm/         TranslateViewModel · HistoryViewModel
```

### مجوز

MIT — فایل [LICENSE](LICENSE) را ببینید.

---

## English

Hamzaban (هم‌زبان, "same language") is a fully offline translator for Android. After a one-time language model download, translation runs entirely on your phone — no text ever leaves the device, no account, no tracking.

### Features

- **59 languages** powered by an on-device neural translation engine.
- **Download once, translate forever**: the first run fetches only the languages you pick; add or remove more whenever you like.
- **Fully offline** once a model is downloaded.
- **History and saved phrases** stored locally in SQLite, with search, swipe-to-delete and bookmarks.
- **Text-to-speech**, copy and share.
- **One-tap language swap** that carries the translation across.
- **Right-to-left done properly**: Vazirmatn typeface, Persian digits, logical layout.
- **Light / dark / system theme**, adjustable text size, Persian and English UI.
- **Quiet by design**: no ads, no analytics, no clutter.

### Privacy

Nothing is collected. The only declared permissions are `INTERNET` and `ACCESS_NETWORK_STATE`, used solely for the one-time model download. Your text, history and settings never leave the device.

### Install

Download the latest APK from [Releases](https://github.com/amirmahdipa77-tech/hamzaban/releases).

- Minimum Android **8.0 (API 26)**
- ABIs: `arm64-v8a`, `armeabi-v7a`

### Build from source

```bash
git clone https://github.com/amirmahdipa77-tech/hamzaban.git
cd hamzaban
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
./gradlew :app:assembleRelease
```

For a signed release, add a `keystore.properties` file at the project root (git-ignored) with `storeFile`, `storePassword`, `keyAlias` and `keyPassword`. Without it the build produces an unsigned APK.

### Architecture

Java 17 + AndroidX (AppCompat / Material Components — no Compose), XML layouts with ViewBinding, lightweight MVVM (`ViewModel` + `LiveData`), plain SQLite, and ML Kit on-device Translation for the engine. No ads, no analytics, no network at translation time.

### License

MIT — see [LICENSE](LICENSE).

---

<div align="center">

Made by **Amirmahdi** · **AM Studio**

</div>
