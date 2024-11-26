package com.school.vacationplanner.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.school.vacationplanner.models.Excursion;
import com.school.vacationplanner.repo.VacationPlannerRepository;
import com.school.vacationplanner.util.NotificationUtility;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;

public class ExcursionNotificationWorker extends Worker {

    // constants
    private static final String TAG = "VacationNotificationWorker";
    private static final String EXCURSION_DATE = "Your excursion is today!";


    // constructor
    public ExcursionNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.d(TAG, "ExcursionNotificationWorker: Worker created");
    }


    // override methods
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: Starting work");
        LocalDate today = LocalDate.now();
        Log.d(TAG, "doWork: Today's date is " + today);
        CountDownLatch latch = new CountDownLatch(1);
        Log.d(TAG, "doWork: CountDownLatch initialized");

        VacationPlannerRepository.getInstance(getApplicationContext()).getAllExcursions(excursions -> {
            Log.d(TAG, "doWork: Retrieved excursions: " + excursions.size());
            for (Excursion e : excursions) {
                if (e.getDate().equals(today)) {
                    Log.d(TAG, "doWork: Excursion starting today: " + e.getTitle());
                    NotificationUtility.showExcursionNotification(getApplicationContext(), e.getTitle(), EXCURSION_DATE);
                }
            }
            latch.countDown();
            Log.d(TAG, "doWork: CountDownLatch decremented");
        });

        try {
            latch.await();
            Log.d(TAG, "doWork: CountDownLatch awaited successfully");
        } catch (InterruptedException e) {
            Log.e(TAG, "doWork: Interrupted while awaiting latch", e);
            return Result.failure();
        }
        Log.d(TAG, "doWork: Work completed successfully");
        return Result.success();
    }
}
