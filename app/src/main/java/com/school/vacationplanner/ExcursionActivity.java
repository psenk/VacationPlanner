package com.school.vacationplanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.school.vacationplanner.adapters.ExcursionAdapter;
import com.school.vacationplanner.fragments.ExcursionDialogFragment;
import com.school.vacationplanner.fragments.VacationDialogFragment;
import com.school.vacationplanner.models.Excursion;
import com.school.vacationplanner.models.Vacation;
import com.school.vacationplanner.repo.VacationPlannerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExcursionActivity extends AppCompatActivity {

    // constants
    private static final String TAG = "ExcursionActivity";
    private static final String EXCURSION_TITLE_BAR = "Excursions";
    private static final String EXCURSION_UPDATED = "Excursion Updated";


    // variables
    private ExcursionAdapter adapter;
    private List<Excursion> excursionList = new ArrayList<>();
    private long vacationId;


    // override methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excursion);
        Log.d(TAG, "onCreate: Activity created");

        vacationId = getIntent().getLongExtra("vacationId", -1);
        Log.d(TAG, "onCreate: Vacation ID = " + vacationId);

        setUpToolbar();
        setUpRecyclerView();
        loadExcursions();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_excursion, menu);
        Log.d(TAG, "onCreateOptionsMenu: Menu created");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: Selected item ID = " + item.getItemId());
        if (item.getItemId() == R.id.action_add_excursion) {
            Log.d(TAG, "onOptionsItemSelected: Add Excursion selected");
            ExcursionDialogFragment addExcursionDialog = new ExcursionDialogFragment(vacationId);
            addExcursionDialog.show(getSupportFragmentManager().beginTransaction(), "NewExcursionDialog");
            addExcursionDialog.setExcursionAddedListener(excursion -> {
                Log.d(TAG, "onOptionsItemSelected: Excursion added, saving to repository");
                VacationPlannerRepository.getInstance(this).addExcursion(excursion, success -> {
                    if (success > 0) {
                        Log.d(TAG, "onOptionsItemSelected: Excursion saved successfully");
                        runOnUiThread(this::loadExcursions);
                    } else {
                        Log.e(TAG, "onOptionsItemSelected: Failed to save excursion");
                    }
                });
            });
            return true;
        } else if (item.getItemId() == R.id.action_preferences) {
            Log.d(TAG, "onOptionsItemSelected: Preferences selected");
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "onOptionsItemSelected: Home button selected, finishing activity");
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // custom methods
    private void setUpRecyclerView() {
        Log.d(TAG, "setUpRecyclerView: Setting up RecyclerView");
        RecyclerView recyclerView = findViewById(R.id.excursion_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExcursionAdapter(this);
        recyclerView.setAdapter(adapter);

        adapter.setOnExcursionClickListener(new ExcursionAdapter.OnExcursionClickListener() {
            @Override
            public void onExcursionEdit(Excursion excursion) {
                Log.d(TAG, "onExcursionEdit: Editing excursion with ID: " + excursion.getId());
                showEditExcursionDialog(excursion);
            }

            @Override
            public void onExcursionDelete(Excursion excursion) {
                Log.d(TAG, "onExcursionDelete: Deleting excursion with ID: " + excursion.getId());
                VacationPlannerRepository.getInstance(ExcursionActivity.this).deleteExcursion(excursion, success -> {
                    if (success) {
                        Log.d(TAG, "onExcursionDelete: Excursion deleted successfully");
                        runOnUiThread(() -> {
                            excursionList.remove(excursion);
                            loadExcursions();
                        });
                    } else {
                        Log.e(TAG, "onExcursionDelete: Failed to delete excursion");
                    }
                });
            }
        });
    }

    private void setUpToolbar() {
        Log.d(TAG, "setUpToolbar: Setting up toolbar");
        Toolbar toolbar = findViewById(R.id.excursion_toolbar);
        toolbar.setTitle(EXCURSION_TITLE_BAR);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void loadExcursions() {
        Log.d(TAG, "loadExcursions: Loading excursions for vacation ID = " + vacationId);
        VacationPlannerRepository.getInstance(this).getExcursionsForVacation(vacationId, excursions -> {
            runOnUiThread(() -> {
                if (excursions != null) {
                    Log.d(TAG, "loadExcursions: Excursions loaded, count = " + excursions.size());
                    excursionList.clear();
                    excursionList.addAll(excursions);
                    adapter.setExcursions(excursions);
                } else {
                    Log.e(TAG, "loadExcursions: Failed to load excursions");
                }
            });
        });
    }

    private void showEditExcursionDialog(Excursion excursion) {
        Log.d(TAG, "showEditExcursionDialog: Editing excursion, excursion ID: " + excursion.getId());
        ExcursionDialogFragment dialog = new ExcursionDialogFragment(vacationId);

        Bundle args = new Bundle();
        args.putString("title", excursion.getTitle());
        args.putString("date", excursion.getDateFormatted());
        args.putLong("excursionId", excursion.getId());
        dialog.setArguments(args);

        dialog.setExcursionAddedListener(updatedExcursion -> {
            Log.d(TAG, "showEditExcursionDialog: Excursion edited");
            excursionList.set(excursionList.indexOf(excursion), updatedExcursion);
            adapter.setExcursions(new ArrayList<>(excursionList));
            VacationPlannerRepository.getInstance(this).editExcursion(updatedExcursion);
            Toast.makeText(this, EXCURSION_UPDATED, Toast.LENGTH_SHORT).show();
        });

        dialog.show(getSupportFragmentManager(), "EditVacationDialog");
    }
}