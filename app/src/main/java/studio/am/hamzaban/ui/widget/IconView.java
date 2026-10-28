package studio.am.hamzaban.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import studio.am.hamzaban.R;

/** An ImageView that renders a bundled Octicon by name. */
public class IconView extends AppCompatImageView {

    private String glyphName;
    private boolean tinted;

    public IconView(Context c) {
        this(c, null);
    }

    public IconView(Context c, AttributeSet attrs) {
        this(c, attrs, 0);
    }

    public IconView(Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);
        init(c, attrs);
    }

    private void init(Context c, AttributeSet attrs) {
        setScaleType(ScaleType.FIT_CENTER);
        setBackground(null);
        if (attrs != null) {
            TypedArray a = null;
            try {
                a = c.obtainStyledAttributes(attrs, R.styleable.IconView);
                String glyph = a.getString(R.styleable.IconView_glyph);
                if (glyph != null) setGlyph(glyph);
            } catch (Throwable ignored) {
            } finally {
                if (a != null) a.recycle();
            }
        }
        if (ImageViewCompat.getImageTintList(this) == null) {
            setGlyphColor(ContextCompat.getColor(c, R.color.text_dim));
        }
    }

    public void setGlyph(String name) {
        glyphName = name;
        try {
            setImageResource(Icons.drawable(name));
        } catch (Throwable ignored) {
        }
    }

    /** Kept for source compatibility; Octicons ship as a single weight. */
    public void setGlyph(String name, boolean filled) {
        setGlyph(name);
    }

    /** Kept for source compatibility. */
    public void setFilled(boolean filled) {
    }

    public void setGlyphColor(int color) {
        try {
            ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(color));
            tinted = true;
        } catch (Throwable ignored) {
        }
    }

    public boolean isTinted() {
        return tinted;
    }

    public String glyphName() {
        return glyphName;
    }
}
