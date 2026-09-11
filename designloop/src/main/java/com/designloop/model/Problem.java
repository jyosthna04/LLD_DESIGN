package com.designloop.model;

import jakarta.persistence.*;

@Entity
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String difficulty;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String requirements;

    // Comma-separated list of class/concept names the deterministic
    // checker looks for, e.g. "Vehicle,ParkingLot,Slot,Ticket"
    @Column(length = 500)
    private String requiredConcepts;

    public Problem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getRequiredConcepts() {
        return requiredConcepts;
    }

    public void setRequiredConcepts(String requiredConcepts) {
        this.requiredConcepts = requiredConcepts;
    }
}
