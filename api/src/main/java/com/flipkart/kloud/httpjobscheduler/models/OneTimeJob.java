package com.flipkart.kloud.httpjobscheduler.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;

/**
 * @understands: One time job which is deleted after execution
 */

@Entity
public class OneTimeJob extends Job {
    @JsonProperty
    private Long triggerTime;

    OneTimeJob() {}

    public OneTimeJob(String name, HttpApi httpApi, Long triggerTime) {
        super(name, httpApi);
        this.triggerTime = triggerTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OneTimeJob that = (OneTimeJob) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "OneTimeJob{" +
                "id=" + id +
                ", name='" + name + "\'" +
                '}';
    }

    @Override
    public JobInstance nextInstance() {
        if(triggerTime > System.currentTimeMillis()) {
            return new JobInstance(this,triggerTime);
        }
        return null;
    }

    @Override
    @JsonIgnore
    public boolean shouldBeSidelined() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isSidelined() {
        return false;
    }
}