package studio.am.hamzaban.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import studio.am.hamzaban.R;
import studio.am.hamzaban.data.Langs;
import studio.am.hamzaban.data.Prefs;
import studio.am.hamzaban.databinding.FragmentTranslateBinding;
import studio.am.hamzaban.util.Fa;
import studio.am.hamzaban.util.Tts;
import studio.am.hamzaban.util.Ui;
import studio.am.hamzaban.vm.TranslateViewModel;

public class TranslateFragment extends Fragment {

    private static final String REQ_SRC = "hz_src";
    private static final String REQ_TGT = "hz_tgt";

    private FragmentTranslateBinding b;
    private TranslateViewModel vm;
    private Prefs prefs;
    private Tts tts;
    private long lastEntryId = -1L;
    private boolean saved;
    private float swapRotation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentTranslateBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        prefs = new Prefs(requireContext());
        vm = new ViewModelProvider(this).get(TranslateViewModel.class);
        tts = new Tts(requireContext());

        Ui.padIme(b.scroll);
        applyLanguages();
        updateCount();
        wire();
        observe();
        listenForPicks();
    }

    private void applyLanguages() {
        String src = prefs.src();
        String tgt = prefs.tgt();
        b.srcCode.setText(Langs.byCode(src).upperCode());
        b.srcName.setText(Langs.displayName(requireContext(), src));
        b.tgtCode.setText(Langs.byCode(tgt).upperCode());
        b.tgtName.setText(Langs.displayName(requireContext(), tgt));
        b.resultLang.setText(Langs.displayName(requireContext(), tgt));
    }

    private String text() {
        Editable e = b.srcInput.getText();
        return e == null ? "" : e.toString();
    }

    private void updateCount() {
        int n = text().length();
        b.charCount.setText(Fa.num(requireContext(), n + " / 5000"));
    }

    private void wire() {
        b.srcInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int c, int d) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int c, int d) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateCount();
            }
        });

        b.btnTranslate.setOnClickListener(v -> {
            Ui.hideKeyboard(requireActivity());
            vm.translate(prefs.src(), prefs.tgt(), text());
        });

        b.btnClear.setOnClickListener(v -> {
            b.srcInput.setText("");
            hideResult();
            vm.reset();
        });

        b.btnSwap.setOnClickListener(v -> {
            String src = prefs.src();
            String tgt = prefs.tgt();
            prefs.setSrc(tgt);
            prefs.setTgt(src);
            swapRotation += 180f;
            b.btnSwap.animate().rotation(swapRotation)
                    .setDuration(240)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            String out = b.resultText.getText() == null ? "" : b.resultText.getText().toString();
            if (b.resultCard.getVisibility() == View.VISIBLE && !out.isEmpty()) {
                b.srcInput.setText(out);
                b.srcInput.setSelection(out.length());
            }
            applyLanguages();
            hideResult();
            vm.reset();
        });

        b.srcPicker.setOnClickListener(v -> openPicker(true));
        b.tgtPicker.setOnClickListener(v -> openPicker(false));

        b.btnCopy.setOnClickListener(v -> copyResult());
        b.btnShare.setOnClickListener(v -> shareResult());
        b.btnSpeak.setOnClickListener(v -> {
            String out = b.resultText.getText() == null ? "" : b.resultText.getText().toString();
            tts.speak(requireContext(), out, prefs.tgt());
        });
        b.btnSave.setOnClickListener(v -> {
            if (lastEntryId <= 0) return;
            saved = !saved;
            vm.setFav(lastEntryId, saved);
            b.btnSave.setFilled(saved);
            b.btnSave.setTextColor(ContextCompat.getColor(requireContext(),
                    saved ? R.color.accent : R.color.text_dim));
            Ui.toast(requireContext(), getString(saved ? R.string.action_saved : R.string.action_removed));
        });
    }

    private void copyResult() {
        String out = b.resultText.getText() == null ? "" : b.resultText.getText().toString();
        if (out.isEmpty()) return;
        try {
            ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("hamzaban", out));
            Ui.toast(requireContext(), getString(R.string.action_copied));
        } catch (Throwable ignored) {
        }
    }

    private void shareResult() {
        String out = b.resultText.getText() == null ? "" : b.resultText.getText().toString();
        if (out.isEmpty()) return;
        try {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, out);
            startActivity(Intent.createChooser(send, getString(R.string.action_share)));
        } catch (Throwable t) {
            Ui.toast(requireContext(), getString(R.string.err_generic));
        }
    }

    private void openPicker(boolean source) {
        try {
            LanguagePickerSheet sheet = LanguagePickerSheet.newInstance(
                    getString(source ? R.string.lang_title_source : R.string.lang_title_target),
                    source ? prefs.src() : prefs.tgt(),
                    source ? "src" : "tgt",
                    source ? REQ_SRC : REQ_TGT);
            sheet.show(getParentFragmentManager(), source ? "pick_src" : "pick_tgt");
        } catch (Throwable ignored) {
        }
    }

    private void listenForPicks() {
        getParentFragmentManager().setFragmentResultListener(REQ_SRC, getViewLifecycleOwner(),
                (key, bundle) -> applyPick(bundle, true));
        getParentFragmentManager().setFragmentResultListener(REQ_TGT, getViewLifecycleOwner(),
                (key, bundle) -> applyPick(bundle, false));
    }

    private void applyPick(Bundle bundle, boolean source) {
        String code = bundle == null ? null : bundle.getString(LanguagePickerSheet.ARG_CODE);
        if (code == null) return;
        if (source) {
            if (code.equals(prefs.tgt())) prefs.setTgt(prefs.src());
            prefs.setSrc(code);
        } else {
            if (code.equals(prefs.src())) prefs.setSrc(prefs.tgt());
            prefs.setTgt(code);
        }
        applyLanguages();
        hideResult();
        vm.reset();
    }

    private void observe() {
        vm.state().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(TranslateViewModel.State s) {
        if (s == null || b == null) return;
        switch (s.kind) {
            case TranslateViewModel.WORKING:
                b.btnTranslate.setEnabled(false);
                b.btnTranslate.setAlpha(0.6f);
                b.btnTranslate.setText(R.string.action_translating);
                b.errorBar.setVisibility(View.GONE);
                break;
            case TranslateViewModel.DONE:
                resetButton();
                b.errorBar.setVisibility(View.GONE);
                lastEntryId = s.entryId;
                saved = false;
                b.btnSave.setFilled(false);
                b.btnSave.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dim));
                b.resultLang.setText(Langs.displayName(requireContext(), prefs.tgt()));
                b.resultText.setText(s.text);
                showResult();
                break;
            case TranslateViewModel.NEEDS_MODEL:
                resetButton();
                b.errorBar.setVisibility(View.VISIBLE);
                b.errorText.setText(getString(R.string.model_missing,
                        Langs.displayName(requireContext(), s.lang)));
                b.btnRetry.setText(R.string.action_download);
                b.btnRetry.setOnClickListener(v -> vm.downloadAndTranslate(s.lang));
                break;
            case TranslateViewModel.ERROR:
                resetButton();
                b.errorBar.setVisibility(View.VISIBLE);
                b.errorText.setText(s.message == null ? getString(R.string.err_generic) : s.message);
                b.btnRetry.setText(R.string.action_retry);
                b.btnRetry.setOnClickListener(v -> vm.translate(prefs.src(), prefs.tgt(), text()));
                break;
            default:
                resetButton();
                break;
        }
    }

    private void resetButton() {
        b.btnTranslate.setEnabled(true);
        b.btnTranslate.setAlpha(1f);
        b.btnTranslate.setText(R.string.action_translate);
    }

    private void showResult() {
        b.emptyState.setVisibility(View.GONE);
        if (b.resultCard.getVisibility() != View.VISIBLE) {
            b.resultCard.setVisibility(View.VISIBLE);
            b.resultCard.setAlpha(0f);
            b.resultCard.setTranslationY(Ui.dp(requireContext(), 12));
            b.resultCard.animate().alpha(1f).translationY(0f)
                    .setDuration(240).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    private void hideResult() {
        b.resultCard.setVisibility(View.GONE);
        b.emptyState.setVisibility(View.VISIBLE);
        lastEntryId = -1L;
        saved = false;
    }

    @Override
    public void onDestroyView() {
        if (tts != null) tts.shutdown();
        tts = null;
        b = null;
        super.onDestroyView();
    }
}
