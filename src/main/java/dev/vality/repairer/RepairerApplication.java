package dev.vality.repairer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class RepairerApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepairerApplication.class, args);
    }
}
