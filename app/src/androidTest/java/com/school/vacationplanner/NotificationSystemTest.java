package com.school.vacationplanner;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.WorkInfo;
import androidx.work.testing.WorkManagerTestInitHelper;

import com.school.vacationplanner.workers.VacationNotificationWorker;
import com.school.vacationplanner.workers.ExcursionNotificationWorker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class NotificationSystemTest {

    private Context context;
    private WorkManager workManager;

    @Before
    public void setUp() {
        // Initialize test context and WorkManager
        context = ApplicationProvider.getApplicationContext();
        WorkManagerTestInitHelper.initializeTestWorkManager(context);
        workManager = WorkManager.getInstance(context);
    }

    @Test
    public void testVacationNotificationWorker() throws Exception {
        // Create a OneTimeWorkRequest for the VacationNotificationWorker
        WorkRequest vacationWorkRequest = new OneTimeWorkRequest.Builder(VacationNotificationWorker.class)
                .setInitialDelay(0, TimeUnit.SECONDS) // Run immediately
                .build();

        // Enqueue the work request
        workManager.enqueue(vacationWorkRequest).getResult().get();

        // Advance time and trigger the work
        WorkManagerTestInitHelper.getTestDriver(context).setAllConstraintsMet(vacationWorkRequest.getId());

        // Check the WorkInfo to verify execution
        WorkInfo workInfo = workManager.getWorkInfoById(vacationWorkRequest.getId()).get();
        assertEquals(WorkInfo.State.SUCCEEDED, workInfo.getState());
    }

    @Test
    public void testExcursionNotificationWorker() throws Exception {
        // Create a OneTimeWorkRequest for the ExcursionNotificationWorker
        WorkRequest excursionWorkRequest = new OneTimeWorkRequest.Builder(ExcursionNotificationWorker.class)
                .setInitialDelay(0, TimeUnit.SECONDS) // Run immediately
                .build();

        // Enqueue the work request
        workManager.enqueue(excursionWorkRequest).getResult().get();

        // Advance time and trigger the work
        WorkManagerTestInitHelper.getTestDriver(context).setAllConstraintsMet(excursionWorkRequest.getId());

        // Check the WorkInfo to verify execution
        WorkInfo workInfo = workManager.getWorkInfoById(excursionWorkRequest.getId()).get();
        assertEquals(WorkInfo.State.SUCCEEDED, workInfo.getState());
    }
}