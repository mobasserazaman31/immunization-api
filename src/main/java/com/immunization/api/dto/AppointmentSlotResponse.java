package com.immunization.api.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AppointmentSlotResponse(LocalDate date, LocalTime time, DayOfWeek dayOfWeek) {

    public static AppointmentSlotResponse fromDateTime(LocalDateTime dateTime){
        return new AppointmentSlotResponse(dateTime.toLocalDate(), dateTime.toLocalTime(), dateTime.getDayOfWeek());
    }
    
}
