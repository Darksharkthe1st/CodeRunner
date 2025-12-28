package com.cr.coderunner;

import java.io.IOException;
import java.util.ArrayList;

public class UserData {
    public String username;
    public int attempts;
    public ArrayList<CodeSubmission> codeAttempts;

    //Constructor for user; Have user start off with no code attempted
    public UserData(String username) {
        this.username = username;
        this.attempts = 0;
        this.codeAttempts = new ArrayList<CodeSubmission>();
    }

    //Add an attempt to the user's history
    public int addAttempt(CodeSubmission code) {
        codeAttempts.add(code);
        return ++attempts;
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
