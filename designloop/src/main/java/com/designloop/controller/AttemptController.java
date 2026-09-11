package com.designloop.controller;

import com.designloop.model.Attempt;
import com.designloop.model.Evaluation;
import com.designloop.model.Submission;
import com.designloop.service.AttemptService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/attempts/{id}")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @GetMapping("/practice")
    public String practice(@PathVariable("id") Long attemptId, Model model) {
        Attempt attempt = attemptService.getAttempt(attemptId);
        Submission submission = attemptService.getSubmission(attempt);
        model.addAttribute("attempt", attempt);
        model.addAttribute("problem", attempt.getProblem());
        model.addAttribute("submission", submission);
        return "practice";
    }

    @PostMapping("/save")
    public String save(@PathVariable("id") Long attemptId,
                        @RequestParam String classesText,
                        @RequestParam(required = false) String interfacesText,
                        @RequestParam(required = false) String relationshipsText,
                        @RequestParam(required = false) String explanationText,
                        @RequestParam(required = false) String tradeoffsText) {
        attemptService.saveDraft(attemptId, classesText, interfacesText, relationshipsText, explanationText, tradeoffsText);
        return "redirect:/attempts/" + attemptId + "/practice?saved=true";
    }

    @PostMapping("/submit")
    public String submit(@PathVariable("id") Long attemptId,
                          @RequestParam String classesText,
                          @RequestParam(required = false) String interfacesText,
                          @RequestParam(required = false) String relationshipsText,
                          @RequestParam(required = false) String explanationText,
                          @RequestParam(required = false) String tradeoffsText) {
        attemptService.submitAndEvaluate(attemptId, classesText, interfacesText, relationshipsText, explanationText, tradeoffsText);
        return "redirect:/attempts/" + attemptId + "/feedback";
    }

    @GetMapping("/feedback")
    public String feedback(@PathVariable("id") Long attemptId, Model model) {
        Attempt attempt = attemptService.getAttempt(attemptId);
        Evaluation evaluation = attemptService.getEvaluation(attempt);
        model.addAttribute("attempt", attempt);
        model.addAttribute("problem", attempt.getProblem());
        model.addAttribute("evaluation", evaluation);
        return "feedback";
    }

    @PostMapping("/retry")
    public String retryEvaluation(@PathVariable("id") Long attemptId) {
        attemptService.retryEvaluation(attemptId);
        return "redirect:/attempts/" + attemptId + "/feedback";
    }
}
