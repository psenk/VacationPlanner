package com.school.vacationplanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.school.vacationplanner.adapters.VacationAdapter;
import com.school.vacationplanner.fragments.VacationDialogFragment;
import com.school.vacationplanner.models.Vacation;
import com.school.vacationplanner.repo.VacationPlannerRepository;
import com.school.vacationplanner.workers.ExcursionNotificationWorker;
import com.school.vacationplanner.workers.VacationNotificationWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VacationActivity extends AppCompatActivity {

    // constants
    private static final String TAG = "VacationActivity";

    private static final String VACATION_ADDED = "Vacation Added";
    private static final String VACATION_UPDATED = "Vacation Updated";
    private static final String VACATION_DELETE_CONFIRMATION = "Are you sure you want to delete this vacation?";
    private static final String VACATION_DELETED = "Vacation deleted";
    private static final String VACATION_DETAILS = "Vacation Details";

    private static final String SELECT_VACATION_EDIT = "Select the vacation you would like to edit";
    private static final String SELECT_VACATION_DELETE = "Select the vacation to delete";

    private static final String INVALID_NO_VACATIONS = "No vacations to delete";
    private static final String INVALID_DELETE_EXCURSIONS_EXIST = "Cannot delete vacation with excursions";
    private static final String INVALID_VACATION_ADDED = "Problem adding vacation";

    private static final String CLIPBOARD_COPY = "Copied to Clipboard";
    private static final String EMAIL_SEND = "Send E-mail";


    // variables
    private VacationAdapter adapter;
    private List<Vacation> vacationList = new ArrayList<>();
    private boolean isEditing = false;
    private boolean isDeleting = false;


    // override methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation);
        Log.d(TAG, "onCreate: VacationActivity created");

        setUpToolbar();
        setUpRecyclerView();
        scheduleVacationCheck();
        scheduleExcursionCheck();

        findViewById(R.id.parent_layout).setOnClickListener(v -> {
            if (isEditing || isDeleting) {
                Log.d(TAG, "onCreate: Canceling mode on outside click");
                toggleEditMode(false);
                isDeleting = false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity recreated");
        reloadVacations();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: Menu created");
        getMenuInflater().inflate(R.menu.menu_vacation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: Selected item ID = " + item.getItemId());
        if (item.getItemId() == R.id.action_add_vacation) {
            Log.d(TAG, "onOptionsItemSelected: Add vacation selected");
            VacationDialogFragment addVacationDialog = new VacationDialogFragment();
            addVacationDialog.show(getSupportFragmentManager().beginTransaction(), "NewVacationDialog");
            addVacationDialog.setVacationAddedListener(vacation -> {
                Log.d(TAG, "onOptionsItemSelected: Vacation added, saving to repository");
                VacationPlannerRepository.getInstance(this).addVacation(vacation, success -> {
                    if (success > 0) {
                        Log.d(TAG, "onOptionsItemSelected: Vacation saved successfully");
                        runOnUiThread(() -> {
                            vacationList.add(vacation);
                            adapter.setVacations(new ArrayList<>(vacationList));
                            runOnUiThread(() -> Toast.makeText(this, VACATION_ADDED, Toast.LENGTH_SHORT).show());
                        });
                    } else {
                        Log.e(TAG, "onOptionsItemSelected: Failed to save vacation");
                        runOnUiThread(() -> Toast.makeText(this, INVALID_VACATION_ADDED, Toast.LENGTH_SHORT).show());
                    }
                });
            });
            return true;

        } else if (item.getItemId() == R.id.action_edit_vacation) {
            Log.d(TAG, "onOptionsItemSelected: Edit vacation selected");
            Toast.makeText(this, SELECT_VACATION_EDIT, Toast.LENGTH_SHORT).show();
            toggleEditMode(true);
            adapter.setEditMode(true);
            return true;

        } else if (item.getItemId() == R.id.action_delete_vacation) {
            Log.d(TAG, "onOptionsItemSelected: Delete vacation selected");
            if (!vacationList.isEmpty()) {
                if (!isDeleting) {
                    Log.d(TAG, "onOptionsItemSelected: Entering delete mode");
                    Toast.makeText(this, SELECT_VACATION_DELETE, Toast.LENGTH_SHORT).show();
                    isDeleting = true;
                    adapter.setOnItemClickListener(this::showDeleteConfirmationDialog);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: Exiting delete mode");
                    isDeleting = false;
                    adapter.setOnItemClickListener(null);
                }
            } else {
                Log.w(TAG, "onOptionsItemSelected: No vacations to delete");
                Toast.makeText(this, INVALID_NO_VACATIONS, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (item.getItemId() == R.id.action_preferences) {
            Log.d(TAG, "onOptionsItemSelected: Preferences selected");
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "onOptionsItemSelected: Home button clicked");
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // custom methods
    private void setUpRecyclerView() {
        Log.d(TAG, "setUpRecyclerView: Setting up RecyclerView");
        RecyclerView recyclerView = findViewById(R.id.vacation_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VacationAdapter(this);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(vacation -> {
            if (isDeleting) {
                Log.d(TAG, "setUpRecyclerView: Vacation clicked for deletion: " + vacation.getTitle());
                showDeleteConfirmationDialog(vacation);
            } else {
                Log.d(TAG, "setUpRecyclerView: Vacation clicked, opening details");
                Intent intent = new Intent(this, VacationDetailActivity.class);
                intent.putExtra("vacationId", vacation.getId());
                startActivity(intent);
            }
        });

        adapter.setOnVacationEditListener(this::showEditVacationDialog);

        adapter.setOnExcursionListener(vacation -> {
            Log.d(TAG, "setUpRecyclerView: Excursions selected, vacation ID: " + vacation.getId());
            Intent intent = new Intent(this, ExcursionActivity.class);
            intent.putExtra("vacationId", vacation.getId());
            startActivity(intent);
        });

        adapter.setOnShareClickListener(this::shareVacationDetails);
    }

    private void setUpToolbar() {
        Log.d(TAG, "setUpRecyclerView: Setting up toolbar");
        Toolbar toolbar = findViewById(R.id.vacation_toolbar);
        setSupportActionBar(toolbar);
    }

    private void toggleEditMode(boolean isEditing) {
        Log.d(TAG, "Function…: Toggling edit mode: " + isEditing);
        this.isEditing = isEditing;
        adapter.setEditMode(isEditing);
    }

    private void showDeleteConfirmationDialog(Vacation vacation) {
        Log.d(TAG, "showDeleteConfirmationDialog: Setting up delete dialog");
        new AlertDialog.Builder(this)
                .setMessage(VACATION_DELETE_CONFIRMATION)
                .setPositiveButton("Yes", (dialog, which) ->
                        VacationPlannerRepository.getInstance(this).deleteVacation(vacation, success ->
                                runOnUiThread(() -> {
                                    if (success) {
                                        Log.d(TAG, "showDeleteConfirmationDialog: Vacation deleted, vacation ID: " + vacation.getId());
                                        vacationList.remove(vacation);
                                        adapter.setVacations(new ArrayList<>(vacationList));
                                        Toast.makeText(this, VACATION_DELETED, Toast.LENGTH_SHORT).show();
                                        isDeleting = false;
                                        adapter.setOnItemClickListener(null);
                                    } else {
                                        Log.w(TAG, "showDeleteConfirmationDialog: Vacation has excursions, cannot delete");
                                        Toast.makeText(this, INVALID_DELETE_EXCURSIONS_EXIST, Toast.LENGTH_SHORT).show();
                                        isDeleting = false;
                                    }
                                })))
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                    isDeleting = false;
                })
                .show();
    }

    private void showEditVacationDialog(Vacation vacation) {
        Log.d(TAG, "showEditVacationDialog: Editing vacation, vacation ID: " + vacation.getId());
        VacationDialogFragment dialog = new VacationDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", vacation.getTitle());
        args.putString("lodging", vacation.getLodging());
        args.putString("startDate", vacation.getStartDate().toString());
        args.putString("endDate", vacation.getEndDate().toString());
        dialog.setArguments(args);

        dialog.setVacationAddedListener(updatedVacation -> {
            Log.d(TAG, "showEditVacationDialog: Vacation edited");
            vacationList.set(vacationList.indexOf(vacation), updatedVacation);
            adapter.setVacations(new ArrayList<>(vacationList));
            VacationPlannerRepository.getInstance(this).editVacation(updatedVacation);
            Toast.makeText(this, VACATION_UPDATED, Toast.LENGTH_SHORT).show();
            toggleEditMode(false);
        });

        dialog.show(getSupportFragmentManager(), "EditVacationDialog");
    }

    private void scheduleVacationCheck() {
        Log.d(TAG, "scheduleVacationCheck: Scheduling vacation check");
        WorkRequest vacationCheckRequest = new OneTimeWorkRequest.Builder(VacationNotificationWorker.class)
                .setInitialDelay(1, TimeUnit.DAYS)
                .addTag("vacation_check")
                .build();

        WorkManager.getInstance(this).enqueue(vacationCheckRequest);
    }

    private void scheduleExcursionCheck() {
        Log.d(TAG, "scheduleExcursionCheck: Scheduling excursion check");
        WorkRequest excursionCheckRequest = new OneTimeWorkRequest.Builder(ExcursionNotificationWorker.class)
                .setInitialDelay(1, TimeUnit.DAYS)
                .addTag("excursion_check")
                .build();

        WorkManager.getInstance(this).enqueue(excursionCheckRequest);
    }

    public void shareVacationDetails(Vacation vacation) {
        Log.d(TAG, "shareVacationDetails: Share vacation selected");

        String shareContent = "Vacation Details:\n" +
                "Title: " + vacation.getTitle() + "\n" +
                "Lodging: " + vacation.getLodging() + "\n" +
                "Start Date: " + vacation.getStartDate() + "\n" +
                "End Date: " + vacation.getEndDate();

        AlertDialog.Builder builder = new AlertDialog.Builder(VacationActivity.this);
        builder.setTitle("Share via:")
                .setItems(new CharSequence[]{"E-mail", "Clipboard", "SMS"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // email
                            Log.d(TAG, "shareVacationDetails: Share via email selected");
                            Intent emailIntent = new Intent(Intent.ACTION_SEND);
                            emailIntent.setType("text/plain");
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, VACATION_DETAILS);
                            emailIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
                            startActivity(Intent.createChooser(emailIntent, EMAIL_SEND));
                            break;
                        case 1: // clipboard
                            Log.d(TAG, "shareVacationDetails: Share via clipboard selected");
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText(VACATION_DETAILS, shareContent);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(VacationActivity.this, CLIPBOARD_COPY, Toast.LENGTH_SHORT).show();
                            break;
                        case 2: // sms
                            Log.d(TAG, "shareVacationDetails: Share via SMS selected");
                            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                            smsIntent.setType("vnd.android-dir/mms-sms");
                            smsIntent.putExtra("sms_body", shareContent);
                            startActivity(smsIntent);
                            break;
                    }
                })
                .show();

    }

    private void reloadVacations() {
        Log.d(TAG, "reloadVacations: Reloading vacations");
        VacationPlannerRepository.getInstance(this).getAllVacations(vacations -> {
            vacationList.clear();
            vacationList.addAll(vacations);
            adapter.setVacations(new ArrayList<>(vacationList));
        });
    }
}