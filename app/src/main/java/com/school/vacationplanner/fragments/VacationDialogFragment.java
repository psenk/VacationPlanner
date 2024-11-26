package com.school.vacationplanner.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.school.vacationplanner.R;
import com.school.vacationplanner.models.Vacation;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VacationDialogFragment extends DialogFragment {

    // constants
    private static final String TAG = "VacationDialogFragment";
    private static final String INVALID_DATE_WARNING = "Invalid date format";
    private static final String INVALID_DATE_ORDER_WARNING = "End date must be after the start date";
    private static final String INVALID_DATE_FORMAT_WARNING = "Please use the correct date format (yyyy-MM-dd)";
    private static final String INVALID_MISSING_DATA = "All fields are required";


    // variables
    private OnVacationAddedListener listener;


    // inner classes
    public interface OnVacationAddedListener {
        void onVacationAdded(Vacation vacation);
    }


    // setters
    // listeners
    public void setVacationAddedListener(OnVacationAddedListener listener) {
        this.listener = listener;
        Log.d(TAG, "setVacationAddedListener: Listener set");
    }


    // override methods
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Dialog creation started");
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_new_vacation_dialog, null);

        EditText titleEditText = view.findViewById(R.id.edit_vacation_title);
        EditText lodgingEditText = view.findViewById(R.id.edit_vacation_lodging);
        EditText startDateEditText = view.findViewById(R.id.edit_vacation_start_date);
        EditText endDateEditText = view.findViewById(R.id.edit_vacation_end_date);

        if (getArguments() != null) {
            Log.d(TAG, "onCreateDialog: Populating fields from arguments");
            titleEditText.setText(getArguments().getString("title"));
            lodgingEditText.setText(getArguments().getString("lodging"));
            startDateEditText.setText(getArguments().getString("startDate"));
            endDateEditText.setText(getArguments().getString("endDate"));
        }

        Button saveButton = view.findViewById(R.id.vacation_save_button);
        Button cancelButton = view.findViewById(R.id.vacation_cancel_button);

        saveButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreateDialog: Save button clicked");
            String title = titleEditText.getText().toString().trim();
            String lodging = lodgingEditText.getText().toString().trim();
            String startDate = startDateEditText.getText().toString().trim();
            String endDate = endDateEditText.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(lodging) || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
                Log.w(TAG, "onCreateDialog: Validation failed - missing fields");
                Toast.makeText(getContext(), INVALID_MISSING_DATA, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidDateFormat(startDate) || !isValidDateFormat(endDate)) {
                Log.w(TAG, "onCreateDialog: Validation failed - invalid date format");
                Toast.makeText(getContext(), INVALID_DATE_FORMAT_WARNING, Toast.LENGTH_SHORT)
                        .show();
            }

            validateAndSave(title, lodging, startDate, endDate);
        });

        cancelButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreateDialog: Cancel button clicked");
            dismiss();
        });

        Log.d(TAG, "onCreateDialog: Dialog created successfully");
        return new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();
    }


    // custom methods
    private boolean isValidDateFormat(String date) {
        Log.d(TAG, "isValidDateFormat: Checking format for date: " + date);
        String regex = "^\\d{4}-\\d{2}-\\d{2}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(date);
        boolean isValid = matcher.matches();
        Log.d(TAG, "isValidDateFormat: Date is valid: " + isValid);
        return isValid;
    }

    private void validateAndSave(String title, String lodging, String startDate, String endDate) {
        LocalDate startDateLocal = LocalDate.parse(startDate);
        LocalDate endDateLocal = LocalDate.parse(endDate);

        if (startDateLocal.isAfter(endDateLocal)) {
            Log.w(TAG, "onCreateDialog: Validation failed - end date before start date");
            Toast.makeText(getContext(), INVALID_DATE_ORDER_WARNING, Toast.LENGTH_SHORT).show();
            return;
        }

        Vacation vacation = new Vacation(title, lodging, startDateLocal, endDateLocal);
        Log.d(TAG, "onCreateDialog: New vacation created: " + vacation);
        if (listener != null) {
            Log.d(TAG, "onCreateDialog: Notifying listener of new vacation");
            listener.onVacationAdded(vacation);
        }
        dismiss();
    }
}