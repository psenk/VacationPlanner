package com.school.vacationplanner.repo;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.school.vacationplanner.models.Excursion;
import com.school.vacationplanner.models.Vacation;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VacationPlannerRepository {

    // constants
    private static final String TAG = "VacationPlannerRepository";
    private static final int NUMBER_OF_THREADS = 4;
    private static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static final String DATABASE_NAME = "vacation_planner.db";


    // variables
    private static VacationPlannerRepository vacationRepo;
    private final VacationDao vacationDao;
    private final ExcursionDao excursionDao;


    // inner classes
    public interface Callback<T> {
        void onComplete(T result);
    }

    // constructors
    public static VacationPlannerRepository getInstance(Context context) {
        Log.d(TAG, "getInstance: Getting instance of VacationPlannerRepository");
        if (vacationRepo == null) {
            vacationRepo = new VacationPlannerRepository(context);
            Log.d(TAG, "getInstance: Created new instance of VacationPlannerRepository");
        }
        return vacationRepo;
    }

    private VacationPlannerRepository(Context context) {
        Log.d(TAG, "VacationPlannerRepository: Initializing database");
        VacationPlannerDatabase database = Room.databaseBuilder(context, VacationPlannerDatabase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
        vacationDao = database.vacationDao();
        excursionDao = database.excursionDao();
    }


    // methods
    public void addVacation(Vacation vacation, Callback<Long> callback) {
        Log.d(TAG, "addVacation: Adding vacation - " + vacation.getTitle());
        databaseExecutor.execute(() -> {
            long id = vacationDao.addVacation(vacation);
            Log.d(TAG, "addVacation: Vacation added with ID " + id);
            callback.onComplete(id);
        });
    }

    public void editVacation(Vacation vacation) {
        Log.d(TAG, "editVacation: Editing vacation - " + vacation.getTitle());
        databaseExecutor.execute(() -> {
            Log.d(TAG, "editVacation: Vacation updated - " + vacation.getTitle());
            vacationDao.updateVacation(vacation);
        });
    }

    public void deleteVacation(Vacation vacation, Callback<Boolean> callback) {
        Log.d(TAG, "deleteVacation: Deleting vacation - " + vacation.getTitle());
        databaseExecutor.execute(() -> {
            List<Excursion> excursions = excursionDao.getExcursionsForVacation(vacation.getId());
            if (excursions == null || excursions.isEmpty()) {
                vacationDao.deleteVacation(vacation);
                Log.d(TAG, "deleteVacation: Vacation deleted - " + vacation.getTitle());
                callback.onComplete(true);
            } else {
                Log.d(TAG, "deleteVacation: Vacation not deleted due to associated excursions");
                callback.onComplete(false);
            }
        });
    }

    public void getVacationById(long vacationId, Callback<Vacation> callback) {
        Log.d(TAG, "getVacationById: Fetching vacation for ID " + vacationId);
        databaseExecutor.execute(() -> {
            Vacation vacation = vacationDao.getVacationById(vacationId);
            Log.d(TAG, "getVacationById: Retrieved vacation " + vacation);
            callback.onComplete(vacation);
        });
    }

    public void getAllVacations(Callback<List<Vacation>> callback) {
        Log.d(TAG, "getAllVacations: Fetching all vacations");
        databaseExecutor.execute(() -> {
            List<Vacation> vacations = vacationDao.getAllVacations();
            Log.d(TAG, "getAllVacations: Retrieved " + vacations.size() + " vacations");
            callback.onComplete(vacations);
        });
    }

    public void addExcursion(Excursion excursion, Callback<Long> callback) {
        Log.d(TAG, "addExcursion: Adding excursion for vacation ID " + excursion.getVacationId());
        databaseExecutor.execute(() -> {
            if (vacationDao.getVacationById(excursion.getVacationId()) != null) {
                long id = excursionDao.addExcursion(excursion);
                Log.d(TAG, "addExcursion: Excursion added with ID " + id);
                callback.onComplete(id);
            } else {
                Log.d(TAG, "addExcursion: No vacation found for ID " + excursion.getVacationId());
                callback.onComplete(-1L);
            }
        });
    }

    public void editExcursion(Excursion excursion) {
        Log.d(TAG, "editExcursion: Editing excursion - " + excursion.getTitle());
        databaseExecutor.execute(() -> {
            Log.d(TAG, "editExcursion: Excursion updated - " + excursion.getTitle());
            excursionDao.updateExcursion(excursion);
        });
    }


    public void deleteExcursion(Excursion excursion, Callback<Boolean> callback) {
        Log.d(TAG, "deleteExcursion: Deleting excursion - " + excursion.getTitle());
        databaseExecutor.execute(() -> {
            excursionDao.deleteExcursion(excursion);
            Log.d(TAG, "deleteExcursion: Excursion deleted - " + excursion.getTitle());
            callback.onComplete(true);
        });
    }

    public void getExcursionsForVacation(long vacationId, Callback<List<Excursion>> callback) {
        Log.d(TAG, "getExcursionsForVacation: Fetching excursions for vacation ID " + vacationId);
        databaseExecutor.execute(() -> {
                    if (vacationDao.getVacationById(vacationId) != null) {
                        List<Excursion> excursions = excursionDao.getExcursionsForVacation(vacationId);
                        Log.d(TAG, "getExcursionsForVacation: Retrieved " + excursions.size() + " excursions for vacation ID " + vacationId);
                        callback.onComplete(excursions);
                    } else {
                        Log.d(TAG, "getExcursionsForVacation: No vacation found for ID " + vacationId);
                    }
                }
        );
    }

    public void getAllExcursions(Callback<List<Excursion>> callback) {
        Log.d(TAG, "getAllExcursions: Fetching all excursions");
        databaseExecutor.execute(() -> {
            List<Excursion> excursions = excursionDao.getAllExcursions();
            Log.d(TAG, "getAllExcursions: Retrieved " + excursions.size() + " excursions");
            callback.onComplete(excursions);
        });
    }
}
