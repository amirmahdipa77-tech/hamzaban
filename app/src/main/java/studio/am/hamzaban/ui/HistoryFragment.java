package studio.am.hamzaban.ui;

import studio.am.hamzaban.R;

public class HistoryFragment extends EntryListFragment {

    @Override
    protected boolean savedOnly() {
        return false;
    }

    @Override
    protected int titleRes() {
        return R.string.history_title;
    }

    @Override
    protected int emptyTitleRes() {
        return R.string.history_empty_title;
    }

    @Override
    protected int emptySubRes() {
        return R.string.history_empty_sub;
    }

    @Override
    protected String emptyGlyph() {
        return "history";
    }

    @Override
    protected boolean showClearAll() {
        return true;
    }
}
