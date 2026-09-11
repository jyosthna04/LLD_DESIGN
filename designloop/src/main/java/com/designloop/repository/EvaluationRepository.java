package com.designloop.repository;

import com.designloop.model.Attempt;
import com.designloop.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByAttempt(Attempt attempt);
}
