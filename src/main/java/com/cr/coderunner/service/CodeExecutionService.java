package com.cr.coderunner.service;

import com.cr.coderunner.model.CodeExecution;
import com.cr.coderunner.model.CodeSubmission;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.UUID;

public class CodeExecutionService {
    //Arbitrarily we pick to have 10 threads executing
    private static final int threadCount = 10;

    private final HashMap<String, CodeExecution> results;
    private final ExecutorService executor;

    public CodeExecutionService() {
        this.executor = Executors.newFixedThreadPool(threadCount);
        this.results = new HashMap<>();
    }

    public String execute(CodeExecution execution) {
        // Get a random UUID
        String executionId = UUID.randomUUID().toString();
        // Ensure that UUID isn't already used
        while  (results.containsKey(executionId)) {
            executionId = UUID.randomUUID().toString();
        }

        //Have the workers get started on our execution
        executor.submit(execution);

        //Save the UUID mapped to the execution for later access
        results.put(executionId, execution);
        return executionId;
    }

    public CodeExecution checkExecution(String executionId) {
        CodeExecution execution = results.get(executionId);

        //Only return the execution if it is done
        if( !execution.done ) {
            return null;
        } else {
            return results.get(executionId);
        }
    }
}
