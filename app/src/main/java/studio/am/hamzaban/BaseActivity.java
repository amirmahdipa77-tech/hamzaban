package studio.am.hamzaban;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import studio.am.hamzaban.util.LocaleHelper;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }
}
