package com.immunization.api.repository;

public interface AppointmentRepositoryCustom {

    void lockForPatientBooking(String patientOhipNumber);
}