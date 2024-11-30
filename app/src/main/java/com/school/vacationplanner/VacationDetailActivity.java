package com.school.vacationplanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.school.vacationplanner.adapters.ExcursionAdapter;
import com.school.vacationplanner.fragments.ExcursionDialogFragment;
import com.school.vacationplanner.models.Vacation;
import com.school.vacationplanner.repo.VacationPlannerRepository;

import java.util.Objects;


public class VacationDetailActivity extends AppCompatActivity {


    // constants
    private static final String TAG = "VacationDetailActivity";
    private static final String DETAILS_TITLE_BAR = "Vacation Details";


    // variables
    private TextView titleTextView, lodgingTextView, startDateTextView, endDateTextView;
    private RecyclerView excursionsRecyclerView;
    private ExcursionAdapter excursionAdapter;


    // override methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_detail);

        Log.d(TAG, "onCreate: VacationDetailActivity started");

        setUpToolbar();

        titleTextView = findViewById(R.id.detail_vacation_title);
        lodgingTextView = findViewById(R.id.detail_vacation_lodging);
        startDateTextView = findViewById(R.id.detail_vacation_start_date);
        endDateTextView = findViewById(R.id.detail_vacation_end_date);
        excursionsRecyclerView = findViewById(R.id.excursions_recycler_view);

        excursionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        excursionAdapter = new ExcursionAdapter(this);
        excursionsRecyclerView.setAdapter(excursionAdapter);

        long vacationId = getIntent().getLongExtra("vacationId", -1);
        if (vacationId == -1) {
            Log.e(TAG, "onCreate: Invalid vacation ID");
            finish();
            return;
        }

        VacationPlannerRepository.getInstance(this).getVacationById(vacationId, vacation -> {
            if (vacation != null) {
                runOnUiThread(() -> displayVacationDetails(vacation));
            } else {
                Log.e(TAG, "onCreate: Vacation not found");
                finish();
            }
        });

        VacationPlannerRepository.getInstance(this).getExcursionsForVacation(vacationId, excursions -> {
            if (excursions != null) {
                runOnUiThread(() -> excursionAdapter.setExcursions(excursions));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: Selected item ID = " + item.getItemId());
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "onOptionsItemSelected: Home button selected, finishing activity");
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // custom methods
    private void displayVacationDetails(Vacation vacation) {
        titleTextView.setText(vacation.getTitle());
        lodgingTextView.setText(vacation.getLodging());
        startDateTextView.setText(vacation.getStartDate().toString());
        endDateTextView.setText(vacation.getEndDate().toString());
    }

    private void setUpToolbar() {
        Log.d(TAG, "setUpToolbar: Setting up toolbar");
        Toolbar toolbar = findViewById(R.id.details_toolbar);
        toolbar.setTitle(DETAILS_TITLE_BAR);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }
}