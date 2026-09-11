package com.designloop.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * The exact JSON shape we ask the AI to reply with. Jackson (the JSON
 * library) reads the AI's JSON text straight into this object.
 *
 * @JsonIgnoreProperties(ignoreUnknown = true) means: if the AI adds
 * an extra field we didn't ask for, ignore it instead of crashing.
 * This is part of how we protect the app from "arbitrary AI output".
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LlmFeedbackDto {

    private int score;
    private List<String> strengths;
    private List<String> problems;
    private List<String> suggestions;
    private String tradeoffs;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getProblems() {
        return problems;
    }

    public void setProblems(List<String> problems) {
        this.problems = problems;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public String getTradeoffs() {
        return tradeoffs;
    }

    public void setTradeoffs(String tradeoffs) {
        this.tradeoffs = tradeoffs;
    }
}
