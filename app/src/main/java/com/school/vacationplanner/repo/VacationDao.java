package com.school.vacationplanner.repo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.school.vacationplanner.models.Vacation;

import java.util.List;

@Dao
public interface VacationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addVacation(Vacation vacation);

    @Update
    void updateVacation(Vacation vacation);

    @Delete
    void deleteVacation(Vacation vacation);

    @Query("SELECT * FROM Vacation")
    List<Vacation> getAllVacations();

    @Query("SELECT * FROM Vacation WHERE id = :vacationId")
    Vacation getVacationById(long vacationId);
}
