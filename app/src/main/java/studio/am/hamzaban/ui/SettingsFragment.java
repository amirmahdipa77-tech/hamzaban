package studio.am.hamzaban.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import studio.am.hamzaban.App;
import studio.am.hamzaban.BuildConfig;
import studio.am.hamzaban.R;
import studio.am.hamzaban.core.Lexicon;
import studio.am.hamzaban.data.Prefs;
import studio.am.hamzaban.databinding.FragmentSettingsBinding;
import studio.am.hamzaban.util.Fa;
import studio.am.hamzaban.util.Ui;

public class SettingsFragment extends Fragment {

    private static final String GITHUB_URL = "https://github.com/amirmahdipa77-tech/hamzaban";

    private FragmentSettingsBinding b;
    private Prefs prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentSettingsBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        prefs = new Prefs(requireContext());

        b.versionText.setText(getString(R.string.about_version,
                Fa.num(requireContext(), BuildConfig.VERSION_NAME)));

        b.segThemeSystem.setOnClickListener(v -> setTheme(0));
        b.segThemeLight.setOnClickListener(v -> setTheme(1));
        b.segThemeDark.setOnClickListener(v -> setTheme(2));
        b.segLangFa.setOnClickListener(v -> setLang("fa"));
        b.segLangEn.setOnClickListener(v -> setLang("en"));
        b.segFontSmall.setOnClickListener(v -> setFont(0));
        b.segFontNormal.setOnClickListener(v -> setFont(1));
        b.segFontLarge.setOnClickListener(v -> setFont(2));

        b.aboutGithub.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)));
            } catch (Throwable t) {
                Ui.toast(requireContext(), getString(R.string.err_generic));
            }
        });

        renderSegments();
        renderData();
    }

    private void renderData() {
        Lexicon lex = Lexicon.ready();
        if (lex == null) {
            b.dataEntries.setText("—");
            b.dataSize.setText("—");
            return;
        }
        b.dataEntries.setText(Fa.num(requireContext(), String.valueOf(lex.recordCount())));
        long kb = Math.max(1, lex.sizeBytes() / 1024L);
        b.dataSize.setText(Fa.num(requireContext(), kb + " KB"));
    }

    private void setTheme(int mode) {
        if (prefs.theme() == mode) return;
        prefs.setTheme(mode);
        renderSegments();
        App app = App.get();
        if (app != null) app.applyTheme();
    }

    private void setLang(String tag) {
        if (tag.equals(prefs.lang())) return;
        prefs.setLang(tag);
        if (getActivity() != null) getActivity().recreate();
    }

    private void setFont(int index) {
        if (prefs.fontIndex() == index) return;
        prefs.setFontIndex(index);
        if (getActivity() != null) getActivity().recreate();
    }

    private void renderSegments() {
        if (b == null || prefs == null) return;
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
            v.setTextColor(ContextCompat.getColor(requireContext(),
                    active ? R.color.on_btn : R.color.text_dim));
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onDestroyView() {
        b = null;
        super.onDestroyView();
    }
}
