package studio.am.hamzaban.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateRemoteModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import studio.am.hamzaban.App;
import studio.am.hamzaban.BuildConfig;
import studio.am.hamzaban.R;
import studio.am.hamzaban.data.Langs;
import studio.am.hamzaban.data.Prefs;
import studio.am.hamzaban.databinding.FragmentSettingsBinding;
import studio.am.hamzaban.ui.widget.IconView;
import studio.am.hamzaban.util.Fa;
import studio.am.hamzaban.util.Ui;

public class SettingsFragment extends Fragment {

    private static final String REQ_MODELS = "hz_models";
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

        b.btnAddLang.setOnClickListener(v -> {
            LanguagePickerSheet sheet = LanguagePickerSheet.newInstance(
                    getString(R.string.models_add), "", "models", REQ_MODELS);
            sheet.show(getParentFragmentManager(), "add_lang");
        });

        b.aboutGithub.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)));
            } catch (Throwable t) {
                Ui.toast(requireContext(), getString(R.string.err_generic));
            }
        });

        getParentFragmentManager().setFragmentResultListener(REQ_MODELS, getViewLifecycleOwner(),
                (key, bundle) -> {
                    String code = bundle == null ? null
                            : bundle.getString(LanguagePickerSheet.ARG_CODE);
                    if (code != null) downloadModel(code);
                });

        renderSegments();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadModels();
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

    private void loadModels() {
        try {
            RemoteModelManager.getInstance().getDownloadedModels(TranslateRemoteModel.class)
                    .addOnSuccessListener(this::renderModels)
                    .addOnFailureListener(e -> renderModels(Collections.emptySet()));
        } catch (Throwable t) {
            renderModels(Collections.emptySet());
        }
    }

    private void renderModels(Set<TranslateRemoteModel> models) {
        if (b == null) return;
        b.modelsContainer.removeAllViews();
        List<String> codes = new ArrayList<>();
        if (models != null) {
            for (TranslateRemoteModel model : models) {
                if (model != null && model.getModelName() != null) codes.add(model.getModelName());
            }
        }
        Collections.sort(codes);
        if (codes.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.models_empty);
            empty.setTextSize(13f);
            empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dim));
            int pad = Ui.dp(requireContext(), 12);
            empty.setPadding(pad, pad, pad, pad);
            Ui.font(empty);
            b.modelsContainer.addView(empty);
            return;
        }
        for (String code : codes) {
            b.modelsContainer.addView(buildModelRow(code));
        }
    }

    private View buildModelRow(final String code) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);
        int pad = Ui.dp(requireContext(), 10);
        row.setPadding(pad, pad, pad, pad);

        TextView name = new TextView(requireContext());
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        name.setLayoutParams(tlp);
        name.setTextSize(14f);
        name.setTextColor(ContextCompat.getColor(requireContext(), R.color.text));
        name.setText(Langs.displayName(requireContext(), code)
                + "  ·  " + Langs.byCode(code).nativeName);
        Ui.font(name);

        IconView delete = new IconView(requireContext());
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(
                Ui.dp(requireContext(), 40), Ui.dp(requireContext(), 40));
        delete.setLayoutParams(ilp);
        delete.setGlyph("delete");
        delete.setTextSize(18f);
        delete.setBackgroundResource(R.drawable.bg_round_ripple);
        delete.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_faint));
        delete.setContentDescription(getString(R.string.action_delete));
        delete.setOnClickListener(v -> removeModel(code));

        row.addView(name);
        row.addView(delete);
        return row;
    }

    private void downloadModel(final String code) {
        Ui.toast(requireContext(), getString(R.string.lang_downloading,
                Langs.displayName(requireContext(), code)));
        try {
            RemoteModelManager.getInstance().download(
                            new TranslateRemoteModel.Builder(code).build(),
                            new com.google.mlkit.common.model.DownloadConditions.Builder().build())
                    .addOnCompleteListener(task -> {
                        if (b != null) loadModels();
                        if (isAdded() && task.isSuccessful()) {
                            Ui.toast(requireContext(), getString(R.string.lang_downloaded));
                        }
                    });
        } catch (Throwable ignored) {
        }
    }

    private void removeModel(String code) {
        try {
            RemoteModelManager.getInstance()
                    .deleteDownloadedModel(new TranslateRemoteModel.Builder(code).build())
                    .addOnCompleteListener(task -> {
                        if (b != null) loadModels();
                    });
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onDestroyView() {
        b = null;
        super.onDestroyView();
    }
}
