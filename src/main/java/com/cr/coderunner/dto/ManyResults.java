package com.cr.coderunner.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

//DTO Class used to store many results
public class ManyResults {
    public List<RunResult> results;

    @JsonCreator
    public ManyResults(@JsonProperty("results") List<RunResult> results) {
        this.results = results;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ManyResults that = (ManyResults) o;
        return results.hashCode() == that.results.hashCode();
    }

    @Override
    public int hashCode() {
        int totalHash = 0;
        for (RunResult r : results) {
            totalHash += r.hashCode();
        }
        return totalHash;
    }
}
