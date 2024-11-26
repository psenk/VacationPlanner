package com.school.vacationplanner.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.school.vacationplanner.models.Vacation;
import com.school.vacationplanner.repo.VacationPlannerRepository;
import com.school.vacationplanner.util.NotificationUtility;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;

public class VacationNotificationWorker extends Worker {

    // constants
    private static final String TAG = "VacationNotificationWorker";
    private static final String VACATION_START = "Your vacation is starting today!";
    private static final String VACATION_END = "Your vacation is ending today!";


    // constructor
    public VacationNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Log.d(TAG, "VacationNotificationWorker: Worker created");
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

        VacationPlannerRepository.getInstance(getApplicationContext()).getAllVacations(vacations -> {
            Log.d(TAG, "doWork: Retrieved vacations: " + vacations.size());
            for (Vacation v : vacations) {
                if (v.getStartDate().equals(today)) {
                    Log.d(TAG, "doWork: Vacation starting today: " + v.getTitle());
                    NotificationUtility.showVacationNotification(getApplicationContext(), v.getTitle(), VACATION_START);
                } else if (v.getEndDate().equals(today)) {
                    Log.d(TAG, "doWork: Vacation ending today: " + v.getTitle());
                    NotificationUtility.showVacationNotification(getApplicationContext(), v.getTitle(), VACATION_END);
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
