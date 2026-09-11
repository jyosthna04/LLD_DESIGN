package com.designloop.repository;

import com.designloop.model.Attempt;
import com.designloop.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByAttempt(Attempt attempt);
}
