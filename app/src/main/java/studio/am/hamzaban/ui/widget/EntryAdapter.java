package studio.am.hamzaban.ui.widget;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import studio.am.hamzaban.R;
import studio.am.hamzaban.data.Entry;
import studio.am.hamzaban.data.Langs;
import studio.am.hamzaban.databinding.ItemEntryBinding;
import studio.am.hamzaban.util.TimeFmt;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.VH> {

    public interface Listener {
        void onFav(Entry entry);

        void onOpen(Entry entry);
    }

    private final List<Entry> items = new ArrayList<>();
    private final Listener listener;

    public EntryAdapter(Listener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submit(List<Entry> next) {
        final List<Entry> old = new ArrayList<>(items);
        final List<Entry> fresh = next == null ? new ArrayList<>() : new ArrayList<>(next);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return old.size();
            }

            @Override
            public int getNewListSize() {
                return fresh.size();
            }

            @Override
            public boolean areItemsTheSame(int o, int n) {
                return old.get(o).id == fresh.get(n).id;
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                Entry a = old.get(o);
                Entry b = fresh.get(n);
                return a.fav == b.fav
                        && Objects.equals(a.body, b.body)
                        && Objects.equals(a.out, b.out)
                        && a.ts == b.ts;
            }
        });
        items.clear();
        items.addAll(fresh);
        diff.dispatchUpdatesTo(this);
    }

    public Entry get(int position) {
        return items.get(position);
    }

    public void remove(Entry entry) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == entry.id) {
                items.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEntryBinding b = ItemEntryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        private final ItemEntryBinding b;

        VH(ItemEntryBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(final Entry e, final Listener listener) {
            b.langPair.setText(Langs.displayName(itemView.getContext(), e.src)
                    + " → " + Langs.displayName(itemView.getContext(), e.tgt));
            b.time.setText(TimeFmt.relative(itemView.getContext(), e.ts));
            b.bodyText.setText(e.body);
            b.outText.setText(e.out);
            b.btnFav.setGlyph("bookmark", e.fav);
            b.btnFav.setTextColor(itemView.getContext().getResources().getColor(
                    e.fav ? R.color.accent_text : R.color.text_faint, itemView.getContext().getTheme()));
            b.btnFav.setOnClickListener(v -> {
                if (listener != null) listener.onFav(e);
            });
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOpen(e);
            });
        }
    }
}
