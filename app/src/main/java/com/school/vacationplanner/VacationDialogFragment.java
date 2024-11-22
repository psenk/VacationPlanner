package com.school.vacationplanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.school.vacationplanner.models.Vacation;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VacationDialogFragment extends DialogFragment {

    private static final String INVALID_DATE_WARNING = "Invalid date format";
    private static final String INVALID_DATE_ORDER_WARNING = "End date must be after the start date";
    private static final String INVALID_DATE_FORMAT_WARNING = "Please use the correct date format (yyyy-MM-dd)";
    private static final String INVALID_MISSING_DATA = "All fields are required";
    private OnVacationAddedListener listener;

    public interface OnVacationAddedListener {
        void onVacationAdded(Vacation vacation);
    }

    public void setVacationAddedListener(OnVacationAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_new_vacation_dialog, null);

        EditText titleEditText = view.findViewById(R.id.edit_vacation_title);
        EditText lodgingEditText = view.findViewById(R.id.edit_vacation_lodging);
        EditText startDateEditText = view.findViewById(R.id.edit_vacation_start_date);
        EditText endDateEditText = view.findViewById(R.id.edit_vacation_end_date);

        if (getArguments() != null) {
            titleEditText.setText(getArguments().getString("title"));
            lodgingEditText.setText(getArguments().getString("lodging"));
            startDateEditText.setText(getArguments().getString("startDate"));
            endDateEditText.setText(getArguments().getString("endDate"));
        }

        Button saveButton = view.findViewById(R.id.save_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);

        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String lodging = lodgingEditText.getText().toString().trim();
            String startDate = startDateEditText.getText().toString().trim();
            String endDate = endDateEditText.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(lodging) || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
                Toast.makeText(getContext(), INVALID_MISSING_DATA, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidDateFormat(startDate) || !isValidDateFormat(endDate)) {
                Toast.makeText(getContext(), INVALID_DATE_FORMAT_WARNING, Toast.LENGTH_SHORT)
                        .show();
            }

            try {
                LocalDate startDateLocal = LocalDate.parse(startDate);
                LocalDate endDateLocal = LocalDate.parse(endDate);

                if (startDateLocal.isAfter(endDateLocal)) {
                    Toast.makeText(getContext(), INVALID_DATE_ORDER_WARNING, Toast.LENGTH_SHORT).show();
                    return;
                }

                Vacation vacation = new Vacation(title, lodging, startDateLocal, endDateLocal);
                if (listener != null) {
                    listener.onVacationAdded(vacation);
                }
                dismiss();
            } catch (Exception e) {
                Toast.makeText(getContext(), INVALID_DATE_WARNING, Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());

        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }

    private boolean isValidDateFormat(String date) {
        String regex = "^\\d{4}-\\d{2}-\\d{2}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(date);
        return matcher.matches();
    }
}