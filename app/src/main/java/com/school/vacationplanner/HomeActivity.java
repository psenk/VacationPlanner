package com.school.vacationplanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {


    // constants
    private static final String TAG = "HomeActivity";


    // override methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(TAG, "onCreate: HomeActivity started");

        // Set a click listener on the root view to navigate to VacationActivity
        View rootView = findViewById(R.id.home_root_view);
        rootView.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: Screen tapped, navigating to VacationActivity");
            Intent intent = new Intent(HomeActivity.this, VacationActivity.class);
            startActivity(intent);
            finish();
        });
    }
}