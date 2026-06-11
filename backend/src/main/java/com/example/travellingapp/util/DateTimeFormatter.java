package com.example.travellingapp.util;

import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
@Log4j2
public class DateTimeFormatter {

    private DateTimeFormatter() {}

    public static LocalDate toLocalDate(String date) {
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(date, formatter);
        } catch (java.time.format.DateTimeParseException e) {
            // If the default parsing fails, try the custom format
            log.error("Failed to parse date: {}. Error: {}", date, e.getMessage());
            throw new IllegalArgumentException("Invalid date format. Expected format: dd/MM/yyyy");
        }
    }

    public static String formatLocalDate(LocalDate date) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }
}
