package studio.am.hamzaban.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

import studio.am.hamzaban.R;

/** A TextView that renders a Material Symbols Rounded glyph. */
public class IconView extends AppCompatTextView {

    private String glyphName;
    private boolean filledGlyph;

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
        setIncludeFontPadding(false);
        setGravity(Gravity.CENTER);
        applyTypeface(false);
        if (attrs == null) return;
        TypedArray a = null;
        try {
            a = c.obtainStyledAttributes(attrs, R.styleable.IconView);
            String glyph = a.getString(R.styleable.IconView_glyph);
            boolean filled = a.getBoolean(R.styleable.IconView_filled, false);
            if (glyph != null) setGlyph(glyph, filled);
        } catch (Throwable ignored) {
        } finally {
            if (a != null) a.recycle();
        }
    }

    private void applyTypeface(boolean filled) {
        try {
            setTypeface(ResourcesCompat.getFont(getContext(),
                    filled ? R.font.icons_filled : R.font.icons_outline));
        } catch (Throwable ignored) {
        }
    }

    public void setGlyph(String name) {
        setGlyph(name, filledGlyph);
    }

    public void setGlyph(String name, boolean filled) {
        glyphName = name;
        filledGlyph = filled;
        applyTypeface(filled);
        try {
            setText(new String(Character.toChars(Icons.codepoint(name))));
        } catch (Throwable ignored) {
        }
    }

    public void setFilled(boolean filled) {
        if (glyphName != null) {
            setGlyph(glyphName, filled);
        } else {
            filledGlyph = filled;
            applyTypeface(filled);
        }
    }
}
