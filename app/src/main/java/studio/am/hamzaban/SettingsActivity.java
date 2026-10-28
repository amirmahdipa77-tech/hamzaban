package studio.am.hamzaban;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import studio.am.hamzaban.core.Lexicon;
import studio.am.hamzaban.core.NmtEngine;
import studio.am.hamzaban.data.Prefs;
import studio.am.hamzaban.databinding.ActivitySettingsBinding;
import studio.am.hamzaban.util.Fa;
import studio.am.hamzaban.util.Ui;

public class SettingsActivity extends BaseActivity {

    private static final String GITHUB_URL = "https://github.com/amirmahdipa77-tech/hamzaban";

    private ActivitySettingsBinding b;
    private Prefs prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Ui.edgeToEdge(this);
        Ui.padTop(b.appBar);
        Ui.padBottom(b.scroll);

        prefs = new Prefs(this);
        b.btnBack.setOnClickListener(v -> finish());

        b.segThemeSystem.setOnClickListener(v -> applyThemeMode(0));
        b.segThemeLight.setOnClickListener(v -> applyThemeMode(1));
        b.segThemeDark.setOnClickListener(v -> applyThemeMode(2));
        b.segLangFa.setOnClickListener(v -> setLang("fa"));
        b.segLangEn.setOnClickListener(v -> setLang("en"));
        b.segFontSmall.setOnClickListener(v -> setFont(0));
        b.segFontNormal.setOnClickListener(v -> setFont(1));
        b.segFontLarge.setOnClickListener(v -> setFont(2));

        b.aboutGithub.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)));
            } catch (Throwable t) {
                Ui.toast(this, getString(R.string.err_generic));
            }
        });

        b.versionText.setText(Fa.num(this, BuildConfig.VERSION_NAME));
        renderSegments();
        renderData();
    }

    private void renderData() {
        Lexicon lex = Lexicon.ready();
        b.dataEntries.setText(lex == null
                ? "—"
                : Fa.num(this, String.valueOf(lex.recordCount())));
        boolean ready = NmtEngine.isReady(prefs.src(), prefs.tgt());
        b.engineState.setText(ready ? R.string.engine_ready : R.string.engine_loading);
    }

    private void applyThemeMode(int mode) {
        if (prefs.theme() == mode) return;
        prefs.setTheme(mode);
        renderSegments();
        App app = App.get();
        if (app != null) app.applyTheme();
    }

    private void setLang(String tag) {
        if (tag.equals(prefs.lang())) return;
        prefs.setLang(tag);
        recreate();
    }

    private void setFont(int index) {
        if (prefs.fontIndex() == index) return;
        prefs.setFontIndex(index);
        recreate();
    }

    private void renderSegments() {
        styleSeg(b.segThemeSystem, prefs.theme() == 0);
        styleSeg(b.segThemeLight, prefs.theme() == 1);
        styleSeg(b.segThemeDark, prefs.theme() == 2);
        styleSeg(b.segLangFa, "fa".equals(prefs.lang()));
        styleSeg(b.segLangEn, "en".equals(prefs.lang()));
        styleSeg(b.segFontSmall, prefs.fontIndex() == 0);
        styleSeg(b.segFontNormal, prefs.fontIndex() == 1);
        styleSeg(b.segFontLarge, prefs.fontIndex() == 2);
    }

    private void styleSeg(TextView v, boolean active) {
        try {
            v.setBackgroundResource(active ? R.drawable.bg_seg_on : R.drawable.bg_seg_off);
            v.setTextColor(ContextCompat.getColor(this,
                    active ? R.color.text : R.color.text_dim));
        } catch (Throwable ignored) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderData();
    }
}
