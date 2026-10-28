package studio.am.hamzaban.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateRemoteModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import studio.am.hamzaban.R;
import studio.am.hamzaban.data.Lang;
import studio.am.hamzaban.data.Langs;
import studio.am.hamzaban.databinding.ItemLanguageBinding;
import studio.am.hamzaban.databinding.SheetLanguageBinding;
import studio.am.hamzaban.util.LocaleHelper;

public class LanguagePickerSheet extends BottomSheetDialogFragment {

    public static final String ARG_CODE = "code";
    public static final String ARG_MODE = "mode";
    private static final String ARG_TITLE = "title";
    private static final String ARG_CURRENT = "current";
    private static final String ARG_REQUEST = "request";

    private SheetLanguageBinding b;
    private final Set<String> downloaded = new HashSet<>();
    private String current = "en";
    private String requestKey = "lang_result";
    private String mode = "src";
    private Adapter adapter;

    public static LanguagePickerSheet newInstance(String title, String current, String mode, String requestKey) {
        LanguagePickerSheet sheet = new LanguagePickerSheet();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CURRENT, current);
        args.putString(ARG_MODE, mode);
        args.putString(ARG_REQUEST, requestKey);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = SheetLanguageBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            b.sheetTitle.setText(args.getString(ARG_TITLE, ""));
            current = args.getString(ARG_CURRENT, "en");
            mode = args.getString(ARG_MODE, "src");
            requestKey = args.getString(ARG_REQUEST, "lang_result");
        }

        adapter = new Adapter();
        b.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.list.setAdapter(adapter);
        adapter.submit(Langs.ALL);

        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.55f);
        ViewGroup.LayoutParams lp = b.list.getLayoutParams();
        lp.height = height;
        b.list.setLayoutParams(lp);

        b.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int c, int d) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int c, int d) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.submit(Langs.search(s == null ? "" : s.toString()));
            }
        });

        loadDownloaded();
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            if (getDialog() instanceof com.google.android.material.bottomsheet.BottomSheetDialog) {
                com.google.android.material.bottomsheet.BottomSheetDialog d =
                        (com.google.android.material.bottomsheet.BottomSheetDialog) getDialog();
                View sheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (sheet != null) {
                    sheet.setBackgroundResource(R.drawable.bg_sheet);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void loadDownloaded() {
        try {
            RemoteModelManager.getInstance().getDownloadedModels(TranslateRemoteModel.class)
                    .addOnSuccessListener(models -> {
                        downloaded.clear();
                        if (models != null) {
                            for (TranslateRemoteModel m : models) downloaded.add(m.getModelName());
                        }
                        if (adapter != null) adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                    });
        } catch (Throwable ignored) {
        }
    }

    private void pick(String code) {
        Bundle result = new Bundle();
        result.putString(ARG_CODE, code);
        result.putString(ARG_MODE, mode);
        try {
            getParentFragmentManager().setFragmentResult(requestKey, result);
        } catch (Throwable ignored) {
        }
        dismissAllowingStateLoss();
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.VH> {

        private final List<Lang> items = new ArrayList<>();
        private final boolean fa = LocaleHelper.isFa(requireContext());

        void submit(List<Lang> next) {
            items.clear();
            if (next != null) items.addAll(next);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemLanguageBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {

            private final ItemLanguageBinding ib;

            VH(ItemLanguageBinding ib) {
                super(ib.getRoot());
                this.ib = ib;
            }

            void bind(final Lang lang) {
                ib.code.setText(lang.upperCode());
                ib.name.setText(lang.name(fa));
                boolean same = lang.nativeName.equals(lang.name(fa));
                ib.nativeName.setText(same ? lang.code : lang.nativeName);
                if (downloaded.contains(lang.code)) {
                    ib.status.setGlyph("check_circle", true);
                    ib.status.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.ok));
                } else {
                    ib.status.setGlyph("download");
                    ib.status.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_faint));
                }
                if (lang.code.equals(current)) {
                    ib.getRoot().setBackgroundResource(R.drawable.bg_accent_pill);
                } else {
                    ib.getRoot().setBackgroundResource(R.drawable.bg_row_ripple);
                }
                ib.getRoot().setOnClickListener(v -> pick(lang.code));
            }
        }
    }
}
