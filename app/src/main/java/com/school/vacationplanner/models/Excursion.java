package com.school.vacationplanner.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.school.vacationplanner.repo.Converters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity(foreignKeys = @ForeignKey(entity = Vacation.class, parentColumns = "id", childColumns = "vacation_id", onDelete = ForeignKey.RESTRICT), indices = {@Index("vacation_id")})
@TypeConverters(Converters.class)
public class Excursion {

    // constants
    @Ignore
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    // variables
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "date")
    private LocalDate date;

    @ColumnInfo(name = "vacation_id")
    private long vacationId;


    // constructor
    public Excursion() {
    }

    @Ignore
    public Excursion(String title, long vacationId, LocalDate date) {
        this.title = title;
        this.vacationId = vacationId;
        this.date = date;
    }


    // getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getVacationId() {
        return vacationId;
    }

    public void setVacationId(long vacationId) {
        this.vacationId = vacationId;
    }

    
    // custom methods
    public String getDateFormatted() {
        return date != null ? date.format(DATE_FORMAT) : "Not Set";
    }
}
