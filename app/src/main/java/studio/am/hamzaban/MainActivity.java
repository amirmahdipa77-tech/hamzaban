package studio.am.hamzaban;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.fragment.app.FragmentTransaction;

import studio.am.hamzaban.databinding.ActivityMainBinding;
import studio.am.hamzaban.ui.HistoryFragment;
import studio.am.hamzaban.ui.SavedFragment;
import studio.am.hamzaban.ui.SettingsFragment;
import studio.am.hamzaban.ui.TranslateFragment;
import studio.am.hamzaban.ui.widget.IconView;
import studio.am.hamzaban.util.Ui;

public class MainActivity extends BaseActivity {

    private static final int TAB_COUNT = 4;

    private ActivityMainBinding b;
    private final Fragment[] fragments = new Fragment[TAB_COUNT];
    private final View[] navItems = new View[TAB_COUNT];
    private final IconView[] navIcons = new IconView[TAB_COUNT];
    private final TextView[] navLabels = new TextView[TAB_COUNT];
    private int current = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Ui.edgeToEdge(this);
        Ui.padTop(b.container);
        Ui.padBottom(b.navBarWrap);

        navItems[0] = b.navItemTranslate;
        navItems[1] = b.navItemHistory;
        navItems[2] = b.navItemSaved;
        navItems[3] = b.navItemSettings;
        navIcons[0] = b.navIconTranslate;
        navIcons[1] = b.navIconHistory;
        navIcons[2] = b.navIconSaved;
        navIcons[3] = b.navIconSettings;
        navLabels[0] = b.navLabelTranslate;
        navLabels[1] = b.navLabelHistory;
        navLabels[2] = b.navLabelSaved;
        navLabels[3] = b.navLabelSettings;

        for (int i = 0; i < TAB_COUNT; i++) {
            final int index = i;
            navItems[i].setOnClickListener(v -> select(index, true));
            fragments[i] = getSupportFragmentManager().findFragmentByTag(tag(index));
        }

        int start = savedInstanceState != null ? savedInstanceState.getInt("tab", 0) : 0;
        select(Math.max(0, Math.min(TAB_COUNT - 1, start)), false);

        b.navBar.addOnLayoutChangeListener((v, l, t, r, bo, ol, ot, or, ob) ->
                positionIndicator(current, false));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", Math.max(0, current));
    }

    private static String tag(int index) {
        return "tab" + index;
    }

    private Fragment create(int index) {
        switch (index) {
            case 1:
                return new HistoryFragment();
            case 2:
                return new SavedFragment();
            case 3:
                return new SettingsFragment();
            default:
                return new TranslateFragment();
        }
    }

    public void select(int index, boolean animate) {
        if (index == current) {
            positionIndicator(index, animate);
            return;
        }
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (current >= 0 && fragments[current] != null) {
                ft.hide(fragments[current]);
                ft.setMaxLifecycle(fragments[current], Lifecycle.State.STARTED);
            }
            if (fragments[index] == null) {
                fragments[index] = create(index);
                ft.add(R.id.container, fragments[index], tag(index));
            } else {
                ft.show(fragments[index]);
            }
            ft.setMaxLifecycle(fragments[index], Lifecycle.State.RESUMED);
            ft.commit();
        } catch (Throwable ignored) {
            return;
        }
        current = index;
        updateNav(index, animate);
    }

    private void updateNav(int index, boolean animate) {
        for (int i = 0; i < TAB_COUNT; i++) {
            boolean active = i == index;
            int color = ContextCompat.getColor(this, active ? R.color.text : R.color.text_dim);
            try {
                navIcons[i].setFilled(active);
                navIcons[i].setTextColor(color);
                navLabels[i].setTextColor(color);
                navLabels[i].setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
            } catch (Throwable ignored) {
            }
        }
        positionIndicator(index, animate);
    }

    private void positionIndicator(int index, boolean animate) {
        if (index < 0 || index >= TAB_COUNT) return;
        final View item = navItems[index];
        final View indicator = b.navIndicator;
        indicator.post(() -> {
            try {
                int itemWidth = item.getWidth();
                int indicatorWidth = indicator.getWidth();
                if (itemWidth <= 0 || indicatorWidth <= 0) return;
                float target = item.getX() + (itemWidth - indicatorWidth) / 2f;
                float translation = target - indicator.getLeft();
                if (animate) {
                    indicator.animate().translationX(translation)
                            .setDuration(240)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                } else {
                    indicator.animate().cancel();
                    indicator.setTranslationX(translation);
                }
            } catch (Throwable ignored) {
            }
        });
    }
}
