package com.school.vacationplanner;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.school.vacationplanner.models.Vacation;
import com.school.vacationplanner.repo.VacationPlannerRepository;

import java.util.ArrayList;
import java.util.List;

public class VacationActivity extends AppCompatActivity {

    // fields
    private VacationAdapter adapter;
    private List<Vacation> vacationList = new ArrayList<>();
    private boolean isEditing = false;


    // override methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation);

        setUpToolbar();
        setUpRecyclerView();
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
                Toast.makeText(this, "Vacation Added", Toast.LENGTH_SHORT).show();
            });
            return true;

        } else if (item.getItemId() == R.id.action_edit_vacation) {
            Toast.makeText(this, "Select the vacation you would like to edit", Toast.LENGTH_SHORT).show();
            toggleEditMode(true);
            adapter.setEditMode(true);
            return true;

        } else if (item.getItemId() == R.id.action_delete_vacation) {
            if (!vacationList.isEmpty()) {
                Toast.makeText(this, "Select the vacation to delete", Toast.LENGTH_SHORT).show();

                adapter.setOnItemClickListener(this::showDeleteConfirmationDialog);
            } else {
                Toast.makeText(this, "No vacations to delete", Toast.LENGTH_SHORT).show();
            }
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

        adapter.setOnItemClickListener(vacation -> Toast.makeText(VacationActivity.this, vacation.getTitle() + " clicked.", Toast.LENGTH_SHORT).show());

        adapter.setOnVacationEditListener(vacation -> {
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
                Toast.makeText(this, "Vacation Updated", Toast.LENGTH_SHORT).show();
                toggleEditMode(false);
            });

            dialog.show(getSupportFragmentManager(), "EditVacationDialog");
        });
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
                .setMessage("Are you sure you want to delete this vacation?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    VacationPlannerRepository.getInstance(this).deleteVacation(vacation, success -> {
                        runOnUiThread(() -> {
                            if (success) {
                                vacationList.remove(vacation);
                                adapter.setVacations(new ArrayList<>(vacationList));
                                Toast.makeText(this, "Vacation deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Cannot delete vacation with excursions", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}