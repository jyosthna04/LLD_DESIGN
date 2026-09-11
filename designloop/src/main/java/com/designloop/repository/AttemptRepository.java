package com.designloop.repository;

import com.designloop.model.Attempt;
import com.designloop.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    List<Attempt> findByProblemOrderByAttemptNumberAsc(Problem problem);
    List<Attempt> findAllByOrderByCreatedAtDesc();
    int countByProblem(Problem problem);
}
