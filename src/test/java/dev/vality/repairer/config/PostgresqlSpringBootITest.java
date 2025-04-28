package dev.vality.repairer.config;

import dev.vality.testcontainers.annotations.DefaultSpringBootTest;
import dev.vality.testcontainers.annotations.postgresql.PostgresqlTestcontainerSingleton;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PostgresqlTestcontainerSingleton
@DefaultSpringBootTest
@TestPropertySource(properties = {
        "spring.main.web-application-type=none",
        "management.server.port=0"
//        "testcontainers.postgresql.tag=17"
})
public @interface PostgresqlSpringBootITest {
}
