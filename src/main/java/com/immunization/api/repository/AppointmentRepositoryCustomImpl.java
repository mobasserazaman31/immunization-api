package com.immunization.api.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AppointmentRepositoryCustomImpl implements AppointmentRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public void lockForPatientBooking(String patientOhipNumber) {
        entityManager.createNativeQuery(
                "SELECT pg_advisory_xact_lock(hashtextextended(:patientOhipNumber, 0))")
                .setParameter("patientOhipNumber", patientOhipNumber)
                .getSingleResult();
    }
}

/**
 * 
1. EntityManager Injection in Interfaces
Standard Spring Data JPA repository interfaces (which extend JpaRepository) cannot have instance fields, and you cannot use constructor injection (like @RequiredArgsConstructor) on an interface. To use the EntityManager directly, you need a class implementation, which is exactly what AppointmentRepositoryCustomImpl provides.

2. Limitations of @Query with PostgreSQL Advisory Locks
While you can use the @Query annotation on repository interfaces for native queries, doing so for PostgreSQL advisory locks (pg_advisory_xact_lock) causes issues:

Return Type Conflict: pg_advisory_xact_lock is a PostgreSQL function called via a SELECT statement, but it returns a void or a dummy value. Spring Data JPA expects SELECT queries to return data mapped to entities, projections, or simple scalars.

@Modifying Restrictions: If you try to use @Modifying to handle a void return type, Spring Data JPA restricts it to INSERT, UPDATE, or DELETE statements—it will reject a SELECT statement.
 */