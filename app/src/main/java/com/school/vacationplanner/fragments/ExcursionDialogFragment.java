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
import com.school.vacationplanner.models.Excursion;
import com.school.vacationplanner.repo.VacationPlannerRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcursionDialogFragment extends DialogFragment {

    // constants
    private static final String TAG = "ExcursionDialogFragment";
    private static final String INVALID_MISSING_DATA = "All fields are required";
    private static final String INVALID_DATE_FORMAT_WARNING = "Please use the correct date format (yyyy-MM-dd)";
    private static final String INVALID_VACATION_NOT_FOUND = "Vacation not found";
    private static final String INVALID_DATE_OUTSIDE_VACATION = "Excursion date must be within vacation dates";
    private static final String INVALID_DATE_WARNING = "Invalid date format";


    // variables
    private ExcursionAddedListener listener;
    private long excursionId = -1;
    private long vacationId;


    // inner classes
    public interface ExcursionAddedListener {
        void onExcursionAdded(Excursion excursion);
    }


    // constructor
    public ExcursionDialogFragment(long vacationId) {
        this.vacationId = vacationId;
        Log.d(TAG, "ExcursionDialogFragment: Initialized with vacationId: " + vacationId);
    }


    // setters
    // listeners
    public void setExcursionAddedListener(ExcursionAddedListener listener) {
        this.listener = listener;
        Log.d(TAG, "ExcursionAddedListener set");
    }


    // override methods
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: Dialog created");
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_new_excursion_dialog, null);

        EditText titleAddText = view.findViewById(R.id.add_excursion_title);
        EditText dateAddText = view.findViewById(R.id.add_excursion_date);

        if (getArguments() != null) {
            String title = getArguments().getString("title");
            String date = getArguments().getString("date");
            excursionId = getArguments().getLong("excursionId", -1);
            titleAddText.setText(title);
            dateAddText.setText(date);
            Log.d(TAG, "onCreateDialog: Received arguments: " + title + ", " + date);
        }

        Button saveButton = view.findViewById(R.id.excursion_save_button);
        Button cancelButton = view.findViewById(R.id.excursion_cancel_button);

        saveButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreateDialog: Save button clicked");
            String title = titleAddText.getText().toString().trim();
            String date = dateAddText.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date)) {
                Log.w(TAG, "onCreateDialog: Validation failed: field is empty");
                Toast.makeText(getContext(), INVALID_MISSING_DATA, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidDateFormat(date)) {
                Log.w(TAG, "onCreateDialog: Validation failed - invalid date format");
                Toast.makeText(getContext(), INVALID_DATE_FORMAT_WARNING, Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            validateAndSave(title, date);
        });

        cancelButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreateDialog: Cancel button clicked");
            dismiss();
        });

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

    private void validateAndSave(String title, String date) {
        if (!isValidDateFormat(date)) {
            Log.w(TAG, "onCreateDialog: Validation failed - invalid date format");
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), INVALID_DATE_FORMAT_WARNING, Toast.LENGTH_SHORT).show()
            );
            return;
        }

        // Get vacation details
        VacationPlannerRepository.getInstance(requireContext()).getVacationById(vacationId, vacation -> {
            if (vacation == null) {
                Log.w(TAG, "onCreateDialog: Vacation not found for ID " + vacationId);
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), INVALID_VACATION_NOT_FOUND, Toast.LENGTH_SHORT).show()
                );
                return;
            }

            try {
                LocalDate dateLocal = LocalDate.parse(date);
                LocalDate startDate = vacation.getStartDate();
                LocalDate endDate = vacation.getEndDate();

                // Validate the excursion date
                if (dateLocal.isBefore(startDate) || dateLocal.isAfter(endDate)) {
                    Log.w(TAG, "onCreateDialog: Date is out of vacation bounds");
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), INVALID_DATE_OUTSIDE_VACATION, Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                // Create the new Excursion
                Excursion excursion = new Excursion(title, vacationId, dateLocal);
                if (excursionId != -1) {
                    excursion.setId(excursionId);
                }
                Log.d(TAG, "onCreateDialog: Creating new Excursion: " + excursion.getId());

                // Notify listener and dismiss only if everything is successful
                if (listener != null) {
                    Log.d(TAG, "onCreateDialog: Notifying listener about new excursion");
                    listener.onExcursionAdded(excursion);
                }
                dismiss();
            } catch (DateTimeParseException e) {
                Log.e(TAG, "Date parsing error: " + e.getMessage());
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), INVALID_DATE_FORMAT_WARNING, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}
