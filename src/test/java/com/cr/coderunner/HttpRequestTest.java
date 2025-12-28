package com.cr.coderunner;

import com.cr.coderunner.dto.RunResult;
import com.cr.coderunner.model.CodeSubmission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.ExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.bind.annotation.RequestBody;

import static org.springframework.test.web.servlet.result.StatusResultMatchersExtensionsKt.isEqualTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public class HttpRequestTest {

    @LocalServerPort
    int port;

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void codeShouldRun() {
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


        //Post submission
        restTestClient.post()
                .uri("http://localhost:%d/submit".formatted(port))
                .body(mySubmission)
                .exchange();

        //Run submission, check equality with expected result
        restTestClient.post()
                .uri("http://localhost:%d/run".formatted(port))
                .body("one")
                .exchange()
                .expectBody(RunResult.class)
                .isEqualTo(new RunResult(
                        false,
                        -1.0,
                        "Hi\n",
                        "",
                        ""
        ));

    }
}
