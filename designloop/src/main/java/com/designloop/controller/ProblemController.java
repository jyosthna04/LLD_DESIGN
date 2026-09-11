package com.designloop.controller;

import com.designloop.model.Attempt;
import com.designloop.model.Evaluation;
import com.designloop.model.Problem;
import com.designloop.service.AttemptService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProblemController {

    private final AttemptService attemptService;

    public ProblemController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    // Controller layer: receives the browser request, calls the service
    // layer for logic, and picks which HTML template to show.

    @GetMapping("/")
    public String home() {
        return "redirect:/problems";
    }

    @GetMapping("/problems")
    public String listProblems(Model model) {
        model.addAttribute("problems", attemptService.getAllProblems());
        return "problems";
    }

    @GetMapping("/problems/{id}")
    public String problemDetails(@PathVariable Long id, Model model) {
        model.addAttribute("problem", attemptService.getProblem(id));
        return "problem-details";
    }

    @PostMapping("/problems/{id}/start")
    public String startAttempt(@PathVariable Long id) {
        Attempt attempt = attemptService.startAttempt(id);
        return "redirect:/attempts/" + attempt.getId() + "/practice";
    }

    @GetMapping("/problems/{id}/history")
    public String history(@PathVariable Long id, Model model) {
        Problem problem = attemptService.getProblem(id);
        List<Attempt> attempts = attemptService.getHistoryForProblem(problem);

        // Build a simple lookup of attemptId -> evaluation so the template
        // can show each attempt's score without extra queries per row.
        Map<Long, Evaluation> evaluations = new HashMap<>();
        List<Attempt> withEval = new ArrayList<>(attempts);
        for (Attempt a : withEval) {
            Evaluation eval = attemptService.getEvaluation(a);
            if (eval != null) {
                evaluations.put(a.getId(), eval);
            }
        }

        model.addAttribute("problem", problem);
        model.addAttribute("attempts", attempts);
        model.addAttribute("evaluations", evaluations);
        return "history";
    }
}
