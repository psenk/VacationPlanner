package com.school.vacationplanner.repo;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.school.vacationplanner.models.Excursion;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Converters {

    // constants
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    // converters
    @TypeConverter
    public static LocalDate fromString(String value) {
        return value == null ? null : LocalDate.parse(value, DATE_FORMAT);
    }

    @TypeConverter
    public static String fromLocalDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMAT);
    }

    @TypeConverter
    public static List<Excursion> toExcursionsList(String excursionsJson) {
        if (excursionsJson == null) {
            return null;
        }

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Excursion>>(){}.getType();
        return gson.fromJson(excursionsJson, listType);
    }
}