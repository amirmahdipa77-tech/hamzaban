package studio.am.hamzaban.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;

import studio.am.hamzaban.R;
import studio.am.hamzaban.util.Ui;

public class RingView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private float progress;
    private float strokeWidth;
    private ValueAnimator animator;

    public RingView(Context c) {
        this(c, null);
    }

    public RingView(Context c, AttributeSet attrs) {
        this(c, attrs, 0);
    }

    public RingView(Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);
        float stroke = Ui.dp(c, 12);
        int arcColor = ContextCompat.getColor(c, R.color.accent);
        int trackColor = ContextCompat.getColor(c, R.color.track);
        if (attrs != null) {
            TypedArray a = null;
            try {
                a = c.obtainStyledAttributes(attrs, R.styleable.RingView);
                progress = a.getFloat(R.styleable.RingView_ringProgress, 0f);
                stroke = a.getDimension(R.styleable.RingView_ringWidth, stroke);
                arcColor = a.getColor(R.styleable.RingView_ringColor, arcColor);
                trackColor = a.getColor(R.styleable.RingView_ringTrack, trackColor);
            } catch (Throwable ignored) {
            } finally {
                if (a != null) a.recycle();
            }
        }
        strokeWidth = stroke;
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(stroke);
        trackPaint.setColor(trackColor);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(stroke);
        arcPaint.setColor(arcColor);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(arcColor);
    }

    public void setProgress(float value) {
        progress = Math.max(0f, Math.min(1f, value));
        invalidate();
    }

    public void setProgressAnimated(float value, long duration) {
        float target = Math.max(0f, Math.min(1f, value));
        if (animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(progress, target);
        animator.setDuration(Math.max(1, duration));
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float inset = strokeWidth / 2f + 1f;
        float size = Math.min(getWidth(), getHeight());
        float left = (getWidth() - size) / 2f + inset;
        float top = (getHeight() - size) / 2f + inset;
        rect.set(left, top, left + size - inset * 2f, top + size - inset * 2f);
        canvas.drawArc(rect, 0f, 360f, false, trackPaint);
        if (progress > 0.001f) {
            float sweep = 360f * progress;
            canvas.drawArc(rect, -90f, sweep, false, arcPaint);
            double rad = Math.toRadians(-90f + sweep);
            float cx = rect.centerX() + (float) (Math.cos(rad) * (rect.width() / 2f));
            float cy = rect.centerY() + (float) (Math.sin(rad) * (rect.height() / 2f));
            canvas.drawCircle(cx, cy, strokeWidth * 0.36f, dotPaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        super.onDetachedFromWindow();
    }
}
