package studio.am.hamzaban;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateRemoteModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import studio.am.hamzaban.data.Langs;
import studio.am.hamzaban.data.Prefs;
import studio.am.hamzaban.databinding.ActivitySetupBinding;
import studio.am.hamzaban.ui.widget.IconView;
import studio.am.hamzaban.util.Fa;
import studio.am.hamzaban.util.Ui;

public class SetupActivity extends BaseActivity {

    private static final int TOTAL_STEPS = 2;

    private ActivitySetupBinding b;
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final List<IconView> stepIcons = new ArrayList<>();
    private final List<TextView> stepLabels = new ArrayList<>();

    private volatile boolean running;
    private boolean completed;
    private boolean retryMode;
    private String src;
    private String tgt;
    private int done;
    private int shownPercent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySetupBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        Ui.edgeToEdge(this);
        Ui.padVertical(b.content);

        Prefs prefs = new Prefs(this);
        src = prefs.src();
        tgt = prefs.tgt();
        if (src.equals(tgt)) {
            tgt = src.equals("fa") ? "en" : "fa";
            prefs.setTgt(tgt);
        }

        b.btnStart.setOnClickListener(v -> {
            if (retryMode) {
                retryMode = false;
                startDownload();
            } else if (completed) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        buildSteps();
        setPercent(0);
        checkExisting();
    }

    private void buildSteps() {
        b.steps.removeAllViews();
        stepIcons.clear();
        stepLabels.clear();
        String[][] pairs = {{src, tgt}, {tgt, src}};
        for (int i = 0; i < pairs.length; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = Ui.dp(this, 10);
            row.setLayoutParams(lp);

            IconView icon = new IconView(this);
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(Ui.dp(this, 20), Ui.dp(this, 20));
            icon.setLayoutParams(ilp);
            icon.setTextSize(16f);
            icon.setTextColor(ContextCompat.getColor(this, R.color.text_faint));
            icon.setGlyph("download");
            icon.setImportantForAccessibility(android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO);

            TextView label = new TextView(this);
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tlp.setMarginStart(Ui.dp(this, 10));
            label.setLayoutParams(tlp);
            label.setTextSize(13f);
            label.setTextColor(ContextCompat.getColor(this, R.color.text_dim));
            Ui.font(label);
            label.setText(getString(R.string.setup_step,
                    Langs.displayName(this, pairs[i][0]), Langs.displayName(this, pairs[i][1])));

            row.addView(icon);
            row.addView(label);
            b.steps.addView(row);
            stepIcons.add(icon);
            stepLabels.add(label);
        }
    }

    private void checkExisting() {
        exec.execute(() -> {
            boolean ok = false;
            try {
                RemoteModelManager manager = RemoteModelManager.getInstance();
                Boolean a = Tasks.await(manager.isModelDownloaded(
                        new TranslateRemoteModel.Builder(src).build()), 15, TimeUnit.SECONDS);
                Boolean c = Tasks.await(manager.isModelDownloaded(
                        new TranslateRemoteModel.Builder(tgt).build()), 15, TimeUnit.SECONDS);
                ok = a != null && a && c != null && c;
            } catch (Throwable ignored) {
            }
            final boolean ready = ok;
            main.post(() -> {
                if (ready) {
                    done = TOTAL_STEPS;
                    setPercent(100);
                    b.ring.setProgress(1f);
                    onComplete();
                } else {
                    startDownload();
                }
            });
        });
    }

    private void startDownload() {
        if (running) return;
        running = true;
        completed = false;
        done = 0;
        b.btnStart.setEnabled(false);
        b.btnStart.setAlpha(0.4f);
        b.btnStart.setText(R.string.setup_cta);
        b.title.setText(R.string.setup_title);
        b.sub.setText(R.string.setup_sub);
        b.ringCheck.animate().alpha(0f).setDuration(150).start();
        b.percent.animate().alpha(1f).setDuration(150).start();
        b.ring.setProgressAnimated(0f, 10);
        setPercent(0);
        buildSteps();

        exec.execute(() -> {
            String[] order = {src, tgt};
            for (int i = 0; i < order.length; i++) {
                if (!running) return;
                final int idx = i;
                main.post(() -> setStepState(idx, true, false));
                try {
                    TranslateRemoteModel model = new TranslateRemoteModel.Builder(order[i]).build();
                    Tasks.await(RemoteModelManager.getInstance().download(
                            model, new DownloadConditions.Builder().build()), 10, TimeUnit.MINUTES);
                } catch (Throwable t) {
                    main.post(this::onFailed);
                    return;
                }
                if (!running) return;
                done++;
                main.post(() -> {
                    setStepState(idx, false, true);
                    animateTo(done);
                });
            }
            main.post(this::onComplete);
        });
    }

    private void animateTo(int completedSteps) {
        float target = completedSteps / (float) TOTAL_STEPS;
        b.ring.setProgressAnimated(target, 460);
        animatePercent(Math.round(target * 100f));
    }

    private void animatePercent(int target) {
        final int from = shownPercent;
        ValueAnimator anim = ValueAnimator.ofInt(from, target);
        anim.setDuration(460);
        anim.addUpdateListener(a -> setPercent((int) a.getAnimatedValue()));
        anim.start();
    }

    private void setPercent(int value) {
        shownPercent = value;
        b.percent.setText(getString(R.string.setup_percent, Fa.n(this, value)));
    }

    private void setStepState(int index, boolean active, boolean finished) {
        if (index < 0 || index >= stepIcons.size()) return;
        IconView icon = stepIcons.get(index);
        TextView label = stepLabels.get(index);
        if (finished) {
            icon.setGlyph("check", true);
            icon.setTextColor(ContextCompat.getColor(this, R.color.ok));
            label.setTextColor(ContextCompat.getColor(this, R.color.text_dim));
        } else if (active) {
            icon.setGlyph("autorenew");
            icon.setTextColor(ContextCompat.getColor(this, R.color.accent));
            label.setTextColor(ContextCompat.getColor(this, R.color.text));
            icon.animate().rotationBy(360f).setDuration(900).start();
        }
    }

    private void onFailed() {
        running = false;
        retryMode = true;
        completed = false;
        b.title.setText(R.string.setup_failed);
        b.btnStart.setText(R.string.action_retry);
        b.btnStart.setEnabled(true);
        b.btnStart.setAlpha(1f);
        for (int i = 0; i < stepIcons.size(); i++) {
            stepIcons.get(i).animate().cancel();
            stepIcons.get(i).setRotation(0f);
            if (i >= done) {
                stepIcons.get(i).setGlyph("cloud_off");
                stepIcons.get(i).setTextColor(ContextCompat.getColor(this, R.color.danger));
            }
        }
    }

    private void onComplete() {
        running = false;
        completed = true;
        retryMode = false;
        new Prefs(this).setSetupDone(true);
        b.ring.setProgressAnimated(1f, 520);
        animatePercent(100);
        b.title.setText(R.string.setup_done_title);
        b.sub.setText(R.string.setup_done_sub);
        b.percent.animate().alpha(0f).setDuration(260).setStartDelay(240).start();
        b.ringCheck.setAlpha(0f);
        b.ringCheck.setScaleX(0.6f);
        b.ringCheck.setScaleY(0.6f);
        b.ringCheck.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setStartDelay(260).setDuration(420).start();
        b.btnStart.setEnabled(true);
        b.btnStart.animate().alpha(1f).setDuration(260).start();
        b.btnStart.setText(R.string.setup_cta);
        for (int i = 0; i < stepIcons.size(); i++) {
            setStepState(i, false, true);
        }
    }

    @Override
    protected void onDestroy() {
        running = false;
        exec.shutdownNow();
        super.onDestroy();
    }
}
