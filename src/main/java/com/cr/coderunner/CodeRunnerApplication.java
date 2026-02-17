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

//    @Bean
//    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
//        return args -> {
//            System.out.println("Let's inspect the beans provided by Spring Boot:");
//
//            String[] beanNames = ctx.getBeanDefinitionNames();
//            Arrays.sort(beanNames);
//
//            for (String beanName : beanNames) {
//                System.out.println(beanName);
//            }
//        };
//    }

}
