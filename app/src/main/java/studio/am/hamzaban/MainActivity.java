package studio.am.hamzaban;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import studio.am.hamzaban.data.Prefs;
import studio.am.hamzaban.databinding.ActivityMainBinding;
import studio.am.hamzaban.util.Tts;
import studio.am.hamzaban.util.Ui;
import studio.am.hamzaban.vm.TranslateViewModel;

public class MainActivity extends BaseActivity {

    private static final long DEBOUNCE_MS = 300L;

    private ActivityMainBinding b;
    private TranslateViewModel vm;
    private Prefs prefs;
    private Tts tts;

    private final Handler debounce = new Handler(Looper.getMainLooper());
    private Runnable pending;
    private float swapRotation;
    private ObjectAnimator pulse;
    private String lastRendered = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Ui.edgeToEdge(this);
        Ui.padTop(b.appBar);
        Ui.padBottom(b.resultScroll);

        prefs = new Prefs(this);
        tts = new Tts(this);
        vm = new ViewModelProvider(this).get(TranslateViewModel.class);

        renderDirection();
        wire();
        startPulse();
        vm.state().observe(this, this::render);
        vm.warmEngine(prefs.src(), prefs.tgt());
    }

    private boolean toEnglish() {
        return "fa".equals(prefs.src());
    }

    private void renderDirection() {
        b.langFrom.setText(toEnglish() ? R.string.lang_fa : R.string.lang_en);
        b.langTo.setText(toEnglish() ? R.string.lang_en : R.string.lang_fa);
    }

    private void startPulse() {
        try {
            pulse = ObjectAnimator.ofFloat(b.liveDot, "alpha", 1f, 0.2f);
            pulse.setDuration(850);
            pulse.setRepeatMode(ValueAnimator.REVERSE);
            pulse.setRepeatCount(ValueAnimator.INFINITE);
            pulse.start();
        } catch (Throwable ignored) {
        }
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
                schedule();
            }
        });

        b.btnSwap.setOnClickListener(v -> swap());
        b.langFrom.setOnClickListener(v -> swap());
        b.langTo.setOnClickListener(v -> swap());
        b.btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        b.btnCopy.setOnClickListener(v -> copyResult());
        b.btnShare.setOnClickListener(v -> shareResult());
        b.btnSpeak.setOnClickListener(v -> tts.speak(this, resultText(), prefs.tgt()));

        b.chip1.setOnClickListener(v -> useSuggestion(getString(R.string.suggest_1)));
        b.chip2.setOnClickListener(v -> useSuggestion(getString(R.string.suggest_2)));
        b.chip3.setOnClickListener(v -> useSuggestion(getString(R.string.suggest_3)));
    }

    private void useSuggestion(String value) {
        b.srcInput.setText(value);
        b.srcInput.setSelection(value.length());
        schedule();
    }

    private void schedule() {
        if (pending != null) debounce.removeCallbacks(pending);
        pending = () -> vm.translate(prefs.src(), prefs.tgt(), text());
        debounce.postDelayed(pending, DEBOUNCE_MS);
    }

    private void swap() {
        boolean toEn = toEnglish();
        String current = resultText();
        if (!current.isEmpty()) {
            b.srcInput.setText(current);
            b.srcInput.setSelection(current.length());
        }
        prefs.setSrc(toEn ? "en" : "fa");
        prefs.setTgt(toEn ? "fa" : "en");
        swapRotation += 180f;
        b.btnSwap.animate().rotation(swapRotation)
                .setDuration(260)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        b.langRail.animate().alpha(0.35f).setDuration(90).withEndAction(() -> {
            renderDirection();
            b.langRail.animate().alpha(1f).setDuration(160).start();
        }).start();
        vm.reset();
        vm.warmEngine(prefs.src(), prefs.tgt());
        schedule();
    }

    private String text() {
        Editable e = b.srcInput.getText();
        return e == null ? "" : e.toString();
    }

    private String resultText() {
        return b.resultText.getText() == null ? "" : b.resultText.getText().toString();
    }

    private void render(TranslateViewModel.State s) {
        if (s == null || b == null) return;
        if (s.kind == TranslateViewModel.IDLE) {
            b.emptyState.setVisibility(View.VISIBLE);
            b.resultScroll.setVisibility(View.GONE);
            lastRendered = "";
            return;
        }
        b.emptyState.setVisibility(View.GONE);
        b.resultScroll.setVisibility(View.VISIBLE);

        int badge;
        if (s.mode == 3) {
            badge = R.string.mode_nmt;
        } else if (s.mode == 1) {
            badge = R.string.mode_phrase;
        } else if (s.mode == 2) {
            badge = R.string.mode_words;
        } else {
            badge = R.string.mode_preparing;
        }
        b.modeBadge.setText(badge);
        b.modeBadge.setVisibility(View.VISIBLE);

        String next = s.text == null ? "" : s.text;
        if (!next.equals(lastRendered)) {
            lastRendered = next;
            b.resultText.setText(next);
            b.resultText.setAlpha(0.25f);
            b.resultText.animate().alpha(1f).setDuration(180)
                    .setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    private void copyResult() {
        String out = resultText();
        if (out.isEmpty()) return;
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("hamzaban", out));
            Ui.toast(this, getString(R.string.action_copied));
        } catch (Throwable ignored) {
        }
    }

    private void shareResult() {
        String out = resultText();
        if (out.isEmpty()) return;
        try {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, out);
            startActivity(Intent.createChooser(send, getString(R.string.action_share)));
        } catch (Throwable t) {
            Ui.toast(this, getString(R.string.err_generic));
        }
    }

    @Override
    protected void onDestroy() {
        if (pending != null) debounce.removeCallbacks(pending);
        if (pulse != null) pulse.cancel();
        if (tts != null) tts.shutdown();
        tts = null;
        super.onDestroy();
    }
}
