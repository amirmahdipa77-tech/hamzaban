package studio.am.hamzaban.ui;

import studio.am.hamzaban.R;

public class SavedFragment extends EntryListFragment {

    @Override
    protected boolean savedOnly() {
        return true;
    }

    @Override
    protected int titleRes() {
        return R.string.saved_title;
    }

    @Override
    protected int emptyTitleRes() {
        return R.string.saved_empty_title;
    }

    @Override
    protected int emptySubRes() {
        return R.string.saved_empty_sub;
    }

    @Override
    protected String emptyGlyph() {
        return "bookmark";
    }

    @Override
    protected boolean showClearAll() {
        return false;
    }
}
