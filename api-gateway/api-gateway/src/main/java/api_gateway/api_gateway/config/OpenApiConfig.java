package api_gateway.api_gateway.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
            .group("user-service")
            .pathsToMatch("/api/users/**")
            .build();
    }

    @Bean
    public GroupedOpenApi dietApi() {
        return GroupedOpenApi.builder()
            .group("diet-service")
            .pathsToMatch("/api/diet/**")
            .build();
    }

    @Bean
    public GroupedOpenApi expenseApi() {
        return GroupedOpenApi.builder()
            .group("expense-service")
            .pathsToMatch("/api/expense/**")
            .build();
    }
}
