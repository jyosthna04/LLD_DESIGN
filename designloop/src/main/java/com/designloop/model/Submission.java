package com.designloop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "attempt_id")
    private Attempt attempt;

    // Free-text areas the learner fills in on the Practice page.
    @Column(length = 4000)
    private String classesText;

    @Column(length = 2000)
    private String interfacesText;

    @Column(length = 2000)
    private String relationshipsText;

    @Column(length = 2000)
    private String explanationText;

    @Column(length = 2000)
    private String tradeoffsText;

    private LocalDateTime submittedAt;

    public Submission() {
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

    public String getClassesText() {
        return classesText;
    }

    public void setClassesText(String classesText) {
        this.classesText = classesText;
    }

    public String getInterfacesText() {
        return interfacesText;
    }

    public void setInterfacesText(String interfacesText) {
        this.interfacesText = interfacesText;
    }

    public String getRelationshipsText() {
        return relationshipsText;
    }

    public void setRelationshipsText(String relationshipsText) {
        this.relationshipsText = relationshipsText;
    }

    public String getExplanationText() {
        return explanationText;
    }

    public void setExplanationText(String explanationText) {
        this.explanationText = explanationText;
    }

    public String getTradeoffsText() {
        return tradeoffsText;
    }

    public void setTradeoffsText(String tradeoffsText) {
        this.tradeoffsText = tradeoffsText;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    // Helper: combine all text fields into one block, used by evaluators.
    @Transient
    public String getFullText() {
        StringBuilder sb = new StringBuilder();
        if (classesText != null) sb.append(classesText).append("\n");
        if (interfacesText != null) sb.append(interfacesText).append("\n");
        if (relationshipsText != null) sb.append(relationshipsText).append("\n");
        if (explanationText != null) sb.append(explanationText).append("\n");
        if (tradeoffsText != null) sb.append(tradeoffsText).append("\n");
        return sb.toString();
    }
}
