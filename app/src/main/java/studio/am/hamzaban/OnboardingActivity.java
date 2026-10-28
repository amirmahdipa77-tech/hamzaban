package studio.am.hamzaban;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import studio.am.hamzaban.core.Lexicon;
import studio.am.hamzaban.data.Prefs;
import studio.am.hamzaban.databinding.ActivityOnboardingBinding;
import studio.am.hamzaban.util.Fa;
import studio.am.hamzaban.util.Ui;

public class OnboardingActivity extends BaseActivity {

    private ActivityOnboardingBinding b;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Ui.edgeToEdge(this);
        Ui.padVertical(b.content);

        b.btnStart.setOnClickListener(v -> {
            new Prefs(this).setOnboarded(true);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        b.statA.setText("—");
        b.statB.setText(Fa.n(this, 0));
        b.statC.setText(Fa.num(this, "100%"));
        loadStats();
        playIntro();
    }

    private void loadStats() {
        final android.content.Context app = getApplicationContext();
        new Thread(() -> {
            String label = "—";
            try {
                Lexicon lex = Lexicon.get(app);
                label = Fa.compact(OnboardingActivity.this, lex.recordCount());
            } catch (Throwable ignored) {
            }
            final String value = label;
            runOnUiThread(() -> {
                if (b != null && !isFinishing()) {
                    b.statA.setText(value);
                }
            });
        }, "hz-stats").start();
    }

    private void playIntro() {
        View[] parts = {b.title, b.sub, b.mark, b.feature1, b.feature2, b.feature3, b.btnStart};
        long delay = 40L;
        for (View v : parts) {
            v.setAlpha(0f);
            v.setTranslationY(Ui.dp(this, 16));
            v.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(delay)
                    .setDuration(420)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            delay += 70L;
        }
    }
}
