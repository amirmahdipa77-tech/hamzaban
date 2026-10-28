package studio.am.hamzaban;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.splashscreen.SplashScreen;

import studio.am.hamzaban.data.Prefs;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        Prefs prefs = new Prefs(this);
        Intent next = prefs.onboarded()
                ? new Intent(this, MainActivity.class)
                : new Intent(this, OnboardingActivity.class);
        try {
            startActivity(next);
        } catch (Throwable ignored) {
        }
        finish();
    }
}
