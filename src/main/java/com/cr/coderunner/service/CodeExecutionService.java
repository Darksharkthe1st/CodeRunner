package com.cr.coderunner.service;

import com.cr.coderunner.dto.RunResult;
import com.cr.coderunner.model.CodeExecution;
import com.cr.coderunner.model.CodeSubmission;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.UUID;

@Service
public class CodeExecutionService {
    //Arbitrarily we pick to have 10 threads executing
    private static final int threadCount = 10;

    private final ConcurrentHashMap<String, CodeExecution> results;
    private final ExecutorService executor;

    public CodeExecutionService() {
        this.executor = Executors.newFixedThreadPool(threadCount);
        this.results = new ConcurrentHashMap<>();
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

    public RunResult checkExecution(String executionId) {
        //Remove unnecessary quotes.
        executionId = executionId.replace("\"", "");

        // Return an empty CodeExecution to indicate it doesn't exist
        if (!results.containsKey(executionId)) {
            return new RunResult(new CodeExecution(null), "NONEXISTENT");
        }

        CodeExecution execution = results.get(executionId);
        //Only return the execution if it is done
        if( !execution.done ) {
            return new RunResult(execution, "RUNNING");
        } else {
            return new RunResult(results.remove(executionId), "FINISHED");
        }
    }

    public String listExecutions() {
        StringBuilder sb = new StringBuilder();
        for (String id : results.keySet()) {
            sb.append("{").append(id).append(" : ").append(results.get(id).done).append("}\n");
        }
        return sb.toString();
    }

    @Scheduled(fixedRate = 30_000)
    public void cleanExecutions() {
        for (String id : results.keySet()) {
            CodeExecution execution = results.get(id);
            if (execution.isExpired()) {
                results.remove(id);
            }
        }
    }
}
