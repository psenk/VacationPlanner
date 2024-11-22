package com.school.vacationplanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import com.school.vacationplanner.models.Vacation;
import com.school.vacationplanner.repo.VacationPlannerRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VacationActivity extends AppCompatActivity {

    // constants
    private static final String VACATION_ADDED = "Vacation Added";
    private static final String VACATION_UPDATED = "Vacation Updated";
    private static final String VACATION_DELETE_CONFIRMATION = "Are you sure you want to delete this vacation?";
    private static final String VACATION_DELETED = "Vacation deleted";

    private static final String SELECT_VACATION_EDIT = "Select the vacation you would like to edit";
    private static final String SELECT_VACATION_DELETE = "Select the vacation to delete";

    private static final String INVALID_NO_VACATIONS = "No vacations to delete";
    private static final String INVALID_DELETE_EXCURSIONS_EXIST = "Cannot delete vacation with excursions";

    // fields
    private VacationAdapter adapter;
    private List<Vacation> vacationList = new ArrayList<>();
    private boolean isEditing = false;
    private boolean isDeleting = false;


    // override methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation);

        setUpToolbar();
        setUpRecyclerView();
        scheduleVacationCheck();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vacation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_add_vacation) {
            VacationDialogFragment addDialog = new VacationDialogFragment();
            addDialog.show(getSupportFragmentManager().beginTransaction(), "NewVacationDialog");
            addDialog.setVacationAddedListener(vacation -> {
                VacationPlannerRepository.getInstance(this).addVacation(vacation);
                vacationList.add(vacation);
                adapter.setVacations(new ArrayList<>(vacationList));
                Toast.makeText(this, VACATION_ADDED, Toast.LENGTH_SHORT).show();
            });
            return true;

        } else if (item.getItemId() == R.id.action_edit_vacation) {
            Toast.makeText(this, SELECT_VACATION_EDIT, Toast.LENGTH_SHORT).show();
            toggleEditMode(true);
            adapter.setEditMode(true);
            return true;

        } else if (item.getItemId() == R.id.action_delete_vacation) {
            if (!vacationList.isEmpty()) {
                Toast.makeText(this, SELECT_VACATION_DELETE, Toast.LENGTH_SHORT).show();
                isDeleting = true;
                adapter.setOnItemClickListener(this::showDeleteConfirmationDialog);
            } else {
                Toast.makeText(this, INVALID_NO_VACATIONS, Toast.LENGTH_SHORT).show();
            }
            return true;

        } else if (item.getItemId() == R.id.action_preferences) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;

        } else if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // custom methods
    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new VacationAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(vacation -> {
            if (isDeleting) {
                showDeleteConfirmationDialog(vacation);
            } else {
                Toast.makeText(VacationActivity.this, vacation.getTitle() + " clicked.", Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setOnVacationEditListener(this::showEditVacationDialog);
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void toggleEditMode(boolean isEditing) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.getMenu().findItem(R.id.action_add_vacation).setVisible(!isEditing);
            toolbar.getMenu().findItem(R.id.action_edit_vacation).setVisible(!isEditing);
            toolbar.getMenu().findItem(R.id.action_delete_vacation).setVisible(!isEditing);
            toolbar.getMenu().findItem(R.id.action_cancel_edit).setVisible(isEditing);
        }
    }

    private void showDeleteConfirmationDialog(Vacation vacation) {
        new AlertDialog.Builder(this)
                .setMessage(VACATION_DELETE_CONFIRMATION)
                .setPositiveButton("Yes", (dialog, which) -> {
                    VacationPlannerRepository.getInstance(this).deleteVacation(vacation, success -> {
                        runOnUiThread(() -> {
                            if (success) {
                                vacationList.remove(vacation);
                                adapter.setVacations(new ArrayList<>(vacationList));
                                Toast.makeText(this, VACATION_DELETED, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, INVALID_DELETE_EXCURSIONS_EXIST, Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void showEditVacationDialog(Vacation vacation) {
        VacationDialogFragment dialog = new VacationDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", vacation.getTitle());
        args.putString("lodging", vacation.getLodging());
        args.putString("startDate", vacation.getStartDate().toString());
        args.putString("endDate", vacation.getEndDate().toString());
        dialog.setArguments(args);

        dialog.setVacationAddedListener(updatedVacation -> {
            vacationList.set(vacationList.indexOf(vacation), updatedVacation);
            adapter.setVacations(new ArrayList<>(vacationList));
            VacationPlannerRepository.getInstance(this).editVacation(updatedVacation);
            Toast.makeText(this, VACATION_UPDATED, Toast.LENGTH_SHORT).show();
            toggleEditMode(false);
        });

        dialog.show(getSupportFragmentManager(), "EditVacationDialog");
    }

    private void scheduleVacationCheck() {
        WorkRequest vacationCheckRequest = new OneTimeWorkRequest.Builder(VacationNotificationWorker.class)
                .setInitialDelay(1, TimeUnit.DAYS)
                .addTag("vacation_check")
                .build();

        WorkManager.getInstance(this).enqueue(vacationCheckRequest);
    }
}