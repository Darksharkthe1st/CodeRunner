package com.cr.coderunner;

import com.cr.coderunner.controller.ProblemController;
import com.cr.coderunner.dto.ManyResults;
import com.cr.coderunner.dto.RunResult;
import com.cr.coderunner.model.CodeSubmission;
import com.cr.coderunner.model.Problem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public class ProblemRequestTest {

    @Autowired
    private ProblemController controller;

    @Test
    void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }

    @LocalServerPort
    int port;

    @Autowired
    private RestTestClient restTestClient;

    @Test
    public void problemShouldRun() {
        //Code to be tested
        String code = """
                class test {
                    public static void main(String[] args) {
                        System.out.println("Hi");
                    }
                }
                """;

        //Create submission with code to submit via post request
        CodeSubmission mySubmission = new CodeSubmission(code, "Java", "one");
//        Problems problems = new Problems(
//                List.of(
//
//                )
//        )

        //Post submission
        restTestClient.post()
                .uri("http://localhost:%d/submit".formatted(port))
                .body(mySubmission)
                .exchange();

        restTestClient.post()
                .uri("http://localhost:%d/add_problem".formatted(port))
                .body(new Problem(
                        new Problem.TestCase[]{
                                new Problem.TestCase("Here: money\n", "money"),
                                new Problem.TestCase("Here: funny\n", "funny"),
                                new Problem.TestCase("Here: SCAMMER\n", "nope"),

                        },
                        "desc",
                        "name"
                ))
                .exchange();

        //Code to be tested
        code = """
                import java.util.Scanner;
                class test {
                public static void main(String[] args) {
                    Scanner s = new Scanner(System.in);
                    System.out.println("Here: " + s.nextLine());
                }
                }
                """;

        ManyResults expRes = new ManyResults(List.of(
                new RunResult(true, -1.0, "Here: money\n", "", "success"),
                new RunResult(true, -1.0, "Here: funny\n", "", "success"),
                new RunResult(false, -1.0, "Here: nope\n", "", "Failure: Incorrect output.\n")
        ));

        RestTestClient.BodySpec<ManyResults, ?> actual = restTestClient.post()
                .uri("http://localhost:%d/try_problem".formatted(port))
                .body(new CodeSubmission(
                  code,
                  "Java",
                  "name"

                ))
                .exchange()
                .expectBody(ManyResults.class);
        System.out.println("ACT: " + actual);
        System.out.println("EXP: " + expRes);

        actual.isEqualTo(expRes);
    }
}
