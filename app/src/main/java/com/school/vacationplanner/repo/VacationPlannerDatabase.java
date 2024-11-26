package com.school.vacationplanner.repo;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.school.vacationplanner.models.Excursion;
import com.school.vacationplanner.models.Vacation;

@Database(entities = {Vacation.class, Excursion.class}, version = 3, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class VacationPlannerDatabase extends RoomDatabase {

    public abstract VacationDao vacationDao();
    public abstract ExcursionDao excursionDao();
}
