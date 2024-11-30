package com.school.vacationplanner.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.school.vacationplanner.R;
import com.school.vacationplanner.models.Excursion;

import java.util.ArrayList;
import java.util.List;

public class ExcursionAdapter extends RecyclerView.Adapter<ExcursionAdapter.ExcursionViewHolder> {

    // constants
    private static final String TAG = "ExcursionAdapter";


    // variables
    private List<Excursion> excursionList = new ArrayList<>();
    private final Context context;
    private OnExcursionClickListener excursionListener;


    // inner classes
    // listeners
    public interface OnExcursionClickListener {
        void onExcursionEdit(Excursion excursion);
        void onExcursionDelete(Excursion excursion);
    }


    // holder
    public static class ExcursionViewHolder extends RecyclerView.ViewHolder {
        private final TextView excursionTitle;
        private final TextView excursionDate;
        private final ImageView optionsButton;


        // constructor
        public ExcursionViewHolder(@NonNull View itemView) {
            super(itemView);
            excursionTitle = itemView.findViewById(R.id.excursion_title);
            excursionDate = itemView.findViewById(R.id.excursion_date);
            optionsButton = itemView.findViewById(R.id.excursion_options);
        }
    }


    // constructor
    public ExcursionAdapter(Context context) {
        this.context = context;
        Log.d(TAG, "ExcursionAdapter initialized");
    }


    // override methods
    @NonNull
    @Override
    public ExcursionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Creating new ViewHolder");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_excursion, parent, false);
        return new ExcursionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExcursionViewHolder holder, int position) {
        Excursion excursion = excursionList.get(position);
        Log.d(TAG, "onBindViewHolder: Binding excursion at position " + position + ", excursion ID:" + excursion.getId());
        holder.excursionTitle.setText(excursion.getTitle());
        holder.excursionDate.setText(excursion.getDateFormatted());

        holder.optionsButton.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder: Options button clicked for excursion ID: " + excursion.getId());
            PopupMenu menu = new PopupMenu(v.getContext(), holder.optionsButton);
            menu.inflate(R.menu.excursion_context_menu);
            menu.setOnMenuItemClickListener(item -> {
                if (excursionListener != null) {
                    if (item.getItemId() == R.id.excursion_edit) {
                        Log.d(TAG, "onBindViewHolder: Edit option selected for excursion ID: " + excursion.getId());
                        excursionListener.onExcursionEdit(excursion);
                        return true;
                    } else if (item.getItemId() == R.id.excursion_delete) {
                        Log.d(TAG, "onBindViewHolder: Delete option selected for excursion ID: " + excursion.getId());
                        excursionListener.onExcursionDelete(excursion);
                        return true;
                    }
                }
                return false;
            });
            menu.show();
        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: Item count: " + excursionList.size());
        return excursionList.size();
    }


    // listener setters
    public void setOnExcursionClickListener(OnExcursionClickListener listener) {
        this.excursionListener = listener;
        Log.d(TAG, "OnExcursionClickListener set");
    }


    // custom methods
    public void setExcursions(List<Excursion> excursions) {
        Log.d(TAG, "setExcursions: Setting excursions: " + excursions.size() + " items");
        new Handler(Looper.getMainLooper()).post(() -> {
            excursionList.clear();
            excursionList.addAll(excursions);
            notifyDataSetChanged();
        });
    }
}
