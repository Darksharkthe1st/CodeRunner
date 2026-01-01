package com.cr.coderunner;

import com.cr.coderunner.controller.ProblemController;
import com.cr.coderunner.model.UserData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProblemController.class)
public class ProblemControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserData userData;

    @Test
    void addAndGet_match() {
        
    }
}
