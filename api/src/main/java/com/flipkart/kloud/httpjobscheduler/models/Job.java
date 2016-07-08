package com.flipkart.kloud.httpjobscheduler.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @understands:  parent entity for ScheduledJob and OneTimeJob
 */

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name="job", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public abstract class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @NotBlank
    @Column(unique = true)
    @JsonProperty
    protected String name;

    @Transient
    @JsonIgnore
    protected boolean isExecuting;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "http_api_id")
    @NotNull
    @JsonProperty
    protected HttpApi httpApi;

    @Transient
    @JsonIgnore
    private int numRuns;

    public Job() {
        this.isExecuting = false;
        this.numRuns = 0;
    }

    public Job(String name, HttpApi httpApi) {
        this();
        this.name = name;
        this.httpApi = httpApi;
    }

    public String getName() {
        return name;
    }

    public HttpApi getHttpApi() {
        return httpApi;
    }

    public synchronized void markAsNotExecuting() {
        isExecuting = false;
    }

    public synchronized boolean markAsExecuting() {

        if(!isExecuting) {
            numRuns += 1;
            isExecuting = true;
            return true;
        }
        return false;
    }

    public boolean sameNameAs(String jobName) {
        return this.name.equals(jobName);
    }

    public abstract JobInstance nextInstance();

    public abstract boolean shouldBeSidelined();

    public abstract boolean isSidelined();
}
