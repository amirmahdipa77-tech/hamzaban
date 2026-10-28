package studio.am.hamzaban.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import studio.am.hamzaban.R;
import studio.am.hamzaban.data.Entry;
import studio.am.hamzaban.databinding.FragmentListBinding;
import studio.am.hamzaban.ui.widget.EntryAdapter;
import studio.am.hamzaban.util.Ui;
import studio.am.hamzaban.vm.HistoryViewModel;

public abstract class EntryListFragment extends Fragment implements EntryAdapter.Listener {

    protected FragmentListBinding b;
    protected HistoryViewModel vm;
    protected EntryAdapter adapter;

    private final Handler debounce = new Handler(Looper.getMainLooper());
    private Runnable pendingQuery;

    protected abstract boolean savedOnly();

    protected abstract int titleRes();

    protected abstract int emptyTitleRes();

    protected abstract int emptySubRes();

    protected abstract String emptyGlyph();

    protected abstract boolean showClearAll();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentListBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(HistoryViewModel.class);

        b.title.setText(getString(titleRes()));
        b.emptyTitle.setText(getString(emptyTitleRes()));
        b.emptySub.setText(getString(emptySubRes()));
        b.emptyIcon.setGlyph(emptyGlyph());
        b.btnClearAll.setVisibility(showClearAll() ? View.VISIBLE : View.GONE);

        adapter = new EntryAdapter(this);
        b.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.list.setAdapter(adapter);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder a,
                                  @NonNull RecyclerView.ViewHolder c) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int direction) {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                Entry entry = adapter.get(pos);
                adapter.remove(entry);
                vm.delete(entry);
            }
        });
        helper.attachToRecyclerView(b.list);

        b.btnClearAll.setOnClickListener(v -> confirmClear());

        b.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int c, int d) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int c, int d) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (pendingQuery != null) debounce.removeCallbacks(pendingQuery);
                final String q = s == null ? "" : s.toString();
                pendingQuery = () -> vm.load(savedOnly(), q);
                debounce.postDelayed(pendingQuery, 220);
            }
        });

        vm.items().observe(getViewLifecycleOwner(), this::render);
    }

    private void confirmClear() {
        try {
            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.history_clear_confirm)
                    .setNegativeButton(R.string.action_cancel, (d, w) -> d.dismiss())
                    .setPositiveButton(R.string.history_clear, (d, w) -> vm.clearAll())
                    .show();
        } catch (Throwable ignored) {
        }
    }

    private void render(List<Entry> items) {
        if (b == null) return;
        adapter.submit(items);
        boolean empty = items == null || items.isEmpty();
        b.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        b.list.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (vm != null) vm.load(savedOnly(), b == null || b.searchInput.getText() == null
                ? "" : b.searchInput.getText().toString());
    }

    @Override
    public void onFav(Entry entry) {
        vm.toggleFav(entry);
    }

    @Override
    public void onOpen(Entry entry) {
        try {
            ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("hamzaban", entry.out));
            Ui.toast(requireContext(), getString(R.string.action_copied));
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onDestroyView() {
        if (pendingQuery != null) debounce.removeCallbacks(pendingQuery);
        b = null;
        super.onDestroyView();
    }
}
