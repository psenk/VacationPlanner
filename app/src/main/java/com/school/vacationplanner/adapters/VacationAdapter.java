package com.school.vacationplanner.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.school.vacationplanner.R;
import com.school.vacationplanner.models.Vacation;
import com.school.vacationplanner.repo.VacationPlannerRepository;

import java.util.ArrayList;
import java.util.List;

public class VacationAdapter extends RecyclerView.Adapter<VacationAdapter.VacationViewHolder> {

    // constants
    private static final String TAG = "VacationAdapter";


    // variables
    private List<Vacation> vacationList = new ArrayList<>();
    private final Context context;
    private OnItemClickListener itemListener;
    private OnVacationEditListener editListener;
    private OnShareClickListener shareListener;
    private OnExcursionClickListener excursionListener;
    private boolean editMode = false;


    // inner classes
    // listeners
    public interface OnItemClickListener {
        void onClick(Vacation vacation);
    }

    public interface OnVacationEditListener {
        void onVacationEdit(Vacation vacation);
    }

    public interface OnShareClickListener {
        void onShareClick(Vacation vacation);
    }

    public interface OnExcursionClickListener {
        void onExcursionClick(Vacation vacation);
    }


    // holder
    static class VacationViewHolder extends RecyclerView.ViewHolder {
        private final TextView vacationTitle, vacationLodging, vacationStartDate, vacationEndDate;
        private final ImageButton shareButton;
        private final Button excursionsButton;


        // constructor
        public VacationViewHolder(@NonNull View itemView) {
            super(itemView);
            vacationTitle = itemView.findViewById(R.id.vacation_title);
            vacationLodging = itemView.findViewById(R.id.vacation_lodging);
            vacationStartDate = itemView.findViewById(R.id.vacation_start_date);
            vacationEndDate = itemView.findViewById(R.id.vacation_end_date);
            shareButton = itemView.findViewById(R.id.share_vacation);
            excursionsButton = itemView.findViewById(R.id.excursions);
        }
    }


    // constructor
    public VacationAdapter(Context context) {
        Log.d(TAG, "VacationAdapter initialized");
        this.context = context;
    }


    // override methods
    @NonNull
    @Override
    public VacationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Creating new ViewHolder");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vacation, parent, false);
        return new VacationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VacationViewHolder holder, int position) {
        Vacation vacation = vacationList.get(position);
        Log.d(TAG, "onBindViewHolder: Binding vacation at position " + position + ", vacation ID:" + vacation.getId());
        holder.vacationTitle.setText(vacation.getTitle());
        holder.vacationLodging.setText(vacation.getLodging());
        holder.vacationStartDate.setText(vacation.getStartDateFormatted());
        holder.vacationEndDate.setText(vacation.getEndDateFormatted());

        holder.shareButton.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder: Share button clicked for vacation ID: " + vacation.getTitle());
            if (shareListener != null) {
                shareListener.onShareClick(vacation);
            }
        });

        holder.excursionsButton.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder: Excursion button clicked for vacation ID: " + vacation.getTitle());
            if (excursionListener != null) {
                excursionListener.onExcursionClick(vacation);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder: Item clicked in edit mode for vacation ID: " + vacation.getTitle());
            if (editMode && editListener != null) {
                editListener.onVacationEdit(vacation);
            } else if (itemListener != null) {
                itemListener.onClick(vacation);
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: Item count: " + vacationList.size());
        return vacationList.size();
    }


    // listener setters
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemListener = listener;
        Log.d(TAG, "OnItemClickListener set");
    }

    public void setOnVacationEditListener(OnVacationEditListener listener) {
        this.editListener = listener;
        Log.d(TAG, "OnVacationEditListener set");
    }

    public void setOnShareClickListener(OnShareClickListener listener) {
        this.shareListener = listener;
        Log.d(TAG, "OnShareClickListener set");
    }

    public void setOnExcursionListener(OnExcursionClickListener listener) {
        this.excursionListener = listener;
        Log.d(TAG, "OnExcursionClickListener set");
    }


    // methods
    public void setVacations(List<Vacation> vacations) {
        Log.d(TAG, "setVacations: Setting vacations: " + vacations.size() + " items");
        VacationPlannerRepository.getInstance(context).getAllVacations(v -> {
            new Handler(Looper.getMainLooper()).post(() -> {
                vacationList.clear();
                vacationList = vacations;
                notifyDataSetChanged();
                Log.d(TAG, "setVacations: Vacations updated and adapter notified");
            });
        });
    }

    public void setEditMode(boolean editMode) {
        Log.d(TAG, "setEditMode: Setting edit mode to: " + editMode);
        this.editMode = editMode;
        notifyDataSetChanged();
    }
}