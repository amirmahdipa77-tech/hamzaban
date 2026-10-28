package studio.am.hamzaban.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public final class Ui {

    private Ui() {
    }

    public static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static void toast(Context c, String msg) {
        try {
            Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
        } catch (Throwable ignored) {
        }
    }

    public static void hideKeyboard(Activity a) {
        try {
            View focus = a.getCurrentFocus();
            if (focus == null) return;
            InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            focus.clearFocus();
        } catch (Throwable ignored) {
        }
    }

    public static void edgeToEdge(Activity a) {
        try {
            Window w = a.getWindow();
            WindowCompat.setDecorFitsSystemWindows(w, false);
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                w.setStatusBarContrastEnforced(false);
                w.setNavigationBarContrastEnforced(false);
            }
            boolean dark = (a.getResources().getConfiguration().uiMode
                    & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                    == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            WindowInsetsControllerCompat ctrl = WindowCompat.getInsetsController(w, w.getDecorView());
            if (ctrl != null) {
                ctrl.setAppearanceLightStatusBars(!dark);
                ctrl.setAppearanceLightNavigationBars(!dark);
            }
        } catch (Throwable ignored) {
        }
    }

    public static void padTop(final View v) {
        final int init = v.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(v, (view, insets) -> {
            Insets i = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(view.getPaddingLeft(), init + i.top, view.getPaddingRight(), view.getPaddingBottom());
            return insets;
        });
    }

    public static void padBottom(final View v) {
        final int init = v.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(v, (view, insets) -> {
            Insets i = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), init + i.bottom);
            return insets;
        });
    }

    public static void padVertical(final View v) {
        final int initTop = v.getPaddingTop();
        final int initBottom = v.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(v, (view, insets) -> {
            Insets i = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(view.getPaddingLeft(), initTop + i.top, view.getPaddingRight(), initBottom + i.bottom);
            return insets;
        });
    }

    public static void font(android.widget.TextView v) {
        try {
            v.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(v.getContext(),
                    studio.am.hamzaban.R.font.vazirmatn));
        } catch (Throwable ignored) {
        }
    }

    public static void padIme(final View v) {
        ViewCompat.setOnApplyWindowInsetsListener(v, (view, insets) -> {
            int ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), Math.max(0, ime));
            return insets;
        });
    }
}
