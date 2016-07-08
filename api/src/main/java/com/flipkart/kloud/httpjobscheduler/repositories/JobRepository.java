package com.flipkart.kloud.httpjobscheduler.repositories;

import com.flipkart.kloud.httpjobscheduler.models.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository<T extends Job> extends JpaRepository<T, Long> {
    T findByName(String jobName);
}
