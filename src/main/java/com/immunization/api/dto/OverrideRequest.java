package com.immunization.api.dto;

import java.time.LocalDate;

public record OverrideRequest(LocalDate date, Long clinicId, boolean closed, String startTime, String endTime) {


    public OverrideRequest {
        if (closed) {
            if (startTime == null) startTime = "00:00";
            if (endTime == null) endTime = "00:00";
        }else{
            if (startTime == null) startTime = "11:00";
            if (endTime == null) endTime = "16:00";
        }

    }

    

}


//update db to allow null values for start_time and end_time in slot_overrides table, if closed is true, then start_time and end_time can be null, otherwise they must be provided.