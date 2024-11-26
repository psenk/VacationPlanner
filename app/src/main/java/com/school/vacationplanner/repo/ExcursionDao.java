package com.school.vacationplanner.repo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.school.vacationplanner.models.Excursion;

import java.util.List;

@Dao
public interface ExcursionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addExcursion(Excursion excursion);

    @Update
    void updateExcursion(Excursion excursion);

    @Delete
    void deleteExcursion(Excursion excursion);

    @Query("SELECT * FROM Excursion")
    List<Excursion> getAllExcursions();

    @Query("SELECT * FROM Excursion WHERE vacation_id = :vacationId ORDER BY id")
    List<Excursion> getExcursionsForVacation(long vacationId);
}
