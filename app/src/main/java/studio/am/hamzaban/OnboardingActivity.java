package studio.am.hamzaban;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import android.view.animation.DecelerateInterpolator;

import studio.am.hamzaban.data.Prefs;
import studio.am.hamzaban.databinding.ActivityOnboardingBinding;
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
            startActivity(new Intent(this, SetupActivity.class));
            finish();
        });

        playIntro();
    }

    private void playIntro() {
        View[] parts = {b.mark, b.title, b.sub, b.feature1, b.feature2, b.feature3, b.btnStart};
        long delay = 40L;
        for (int i = 0; i < parts.length; i++) {
            View v = parts[i];
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
        b.mark.setScaleX(0.7f);
        b.mark.setScaleY(0.7f);
        b.mark.animate().scaleX(1f).scaleY(1f).setDuration(520)
                .setInterpolator(new DecelerateInterpolator()).start();
    }
}
