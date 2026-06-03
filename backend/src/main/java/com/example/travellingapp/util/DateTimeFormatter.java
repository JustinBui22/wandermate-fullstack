package com.example.travellingapp.util;

import java.time.LocalDate;

public class DateTimeFormatter {

    private DateTimeFormatter() {}

    public static LocalDate toLocalDate(String date) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.parse(date, formatter);
    }

    public static String formatLocalDate(LocalDate date) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }
}
