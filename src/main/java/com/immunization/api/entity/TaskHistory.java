package com.immunization.api.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "task_history")
@Getter
@NoArgsConstructor
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", nullable = false, unique = true, length = 150)
    private String taskName;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    public TaskHistory(String taskName, Instant completedAt) {
        this.taskName = taskName;
        this.completedAt = completedAt;
    }
}
