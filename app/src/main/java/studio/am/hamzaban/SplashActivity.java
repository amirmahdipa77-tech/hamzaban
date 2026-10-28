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
        route();
    }

    private void route() {
        Prefs prefs = new Prefs(this);
        Intent next;
        if (!prefs.onboarded()) {
            next = new Intent(this, OnboardingActivity.class);
        } else if (!prefs.setupDone()) {
            next = new Intent(this, SetupActivity.class);
        } else {
            next = new Intent(this, MainActivity.class);
        }
        try {
            startActivity(next);
        } catch (Throwable ignored) {
        }
        finish();
    }
}
