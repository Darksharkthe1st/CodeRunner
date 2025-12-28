package com.cr.coderunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class UserData {
    public String username;
    public int attempts;
    public ArrayList<CodeSubmission> codeAttempts;
    public HashMap<String, Problem> problems;

    //Constructor for user; Have user start off with no code attempted
    public UserData(String username) {
        this.username = username;
        this.attempts = 0;
        this.codeAttempts = new ArrayList<CodeSubmission>();
        problems = new HashMap<String, Problem>();
    }

    //Add an attempt to the user's history
    public int addAttempt(CodeSubmission code) {
        codeAttempts.add(code);
        return ++attempts;
    }

    public void addProblem(Problem p) {
        problems.put(p.name, p);
    }

    //Returns the last element of the codeSubmissions array
    public CodeSubmission getLastSubmission() {
        //Only return a submission if it exists
        if (codeAttempts == null || codeAttempts.isEmpty()) {
            return null;
        } else {
            return codeAttempts.getLast();
        }
    }
}
