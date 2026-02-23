package com.cr.coderunner;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

//import io.micrometer.core.aop.TimedAspect;
//import org.springframework.context.annotation.Bean;
//import io.micrometer.core.instrument.MeterRegistry;

@SpringBootApplication
@EnableScheduling
public class CodeRunnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeRunnerApplication.class, args);
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

}
