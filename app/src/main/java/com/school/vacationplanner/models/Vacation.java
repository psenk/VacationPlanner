package com.school.vacationplanner.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.school.vacationplanner.repo.Converters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@TypeConverters(Converters.class)
public class Vacation {

    // fields
    @Ignore
    DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "lodging")
    private String lodging;

    @ColumnInfo(name = "start_date")
    private LocalDate startDate;

    @ColumnInfo(name = "end_date")
    private LocalDate endDate;

    @ColumnInfo(name = "excursions")
    private List<Excursion> excursions;

    // constructors
    public Vacation() {
    }

    @Ignore
    public Vacation(String title, String lodging, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.lodging = lodging;
        this.startDate = startDate;
        this.endDate = endDate;
        this.excursions = new ArrayList<>();
    }

    // getters and setters
    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLodging() {
        return this.lodging;
    }

    public void setLodging(String lodging) {
        this.lodging = lodging;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<Excursion> getExcursions() {
        return excursions;
    }

    public void setExcursions(List<Excursion> excursions) {
        this.excursions = excursions;
    }

    // methods
    public String getStartDateFormatted() {
        return startDate != null ? startDate.format(DATE_FORMAT) : "Not Set";
    }

    public String getEndDateFormatted() {
        return endDate != null ? endDate.format(DATE_FORMAT) : "Not Set";
    }

    public void addExcursion(Excursion excursion) {
        this.excursions.add(excursion);
    }
}