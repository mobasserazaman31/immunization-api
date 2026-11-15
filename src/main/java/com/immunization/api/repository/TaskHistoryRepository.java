package com.immunization.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.immunization.api.entity.TaskHistory;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {

    boolean existsByTaskName(String taskName);
}
