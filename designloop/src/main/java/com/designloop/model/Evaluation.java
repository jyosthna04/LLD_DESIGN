package com.designloop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "evaluations")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "attempt_id")
    private Attempt attempt;

    private int overallScore;

    @Column(length = 3000)
    private String strengths;

    @Column(length = 3000)
    private String problemsFound;

    @Column(length = 3000)
    private String suggestions;

    @Column(length = 1000)
    private String missingRequirements;

    @Column(length = 1000)
    private String tradeoffsNote;

    // Was the AI part of the evaluation reachable? If not, this stays
    // deterministic-only, and we say so on the feedback page.
    private boolean aiAvailable;

    private LocalDateTime createdAt;

    public Evaluation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Attempt getAttempt() {
        return attempt;
    }

    public void setAttempt(Attempt attempt) {
        this.attempt = attempt;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(int overallScore) {
        this.overallScore = overallScore;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getProblemsFound() {
        return problemsFound;
    }

    public void setProblemsFound(String problemsFound) {
        this.problemsFound = problemsFound;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }

    public String getMissingRequirements() {
        return missingRequirements;
    }

    public void setMissingRequirements(String missingRequirements) {
        this.missingRequirements = missingRequirements;
    }

    public String getTradeoffsNote() {
        return tradeoffsNote;
    }

    public void setTradeoffsNote(String tradeoffsNote) {
        this.tradeoffsNote = tradeoffsNote;
    }

    public boolean isAiAvailable() {
        return aiAvailable;
    }

    public void setAiAvailable(boolean aiAvailable) {
        this.aiAvailable = aiAvailable;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // The fields above are stored as a single newline-joined String (simple
    // to persist as a DB column). These derived, non-persisted getters split
    // that back into a list *in Java*, where "\n" is an unambiguous real
    // line-break character. The Feedback page uses these instead of asking
    // the template engine to split the string itself, which is not safe:
    // Thymeleaf/SpEL expressions do not interpret "\n" the way Java does, so
    // a template-side split can silently break on the wrong character.
    @Transient
    public List<String> getStrengthsList() {
        return splitLines(strengths);
    }

    @Transient
    public List<String> getProblemsFoundList() {
        return splitLines(problemsFound);
    }

    @Transient
    public List<String> getSuggestionsList() {
        return splitLines(suggestions);
    }

    @Transient
    public List<String> getMissingRequirementsList() {
        return splitLines(missingRequirements);
    }

    private List<String> splitLines(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
    }
}
