package com.god.life.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {


    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("GodLife API")
                .description("GodLife API 명세 ")
                .version("1.0.0");
    }

//    @Bean
//    public OpenApiCustomizer globalHeaderCustomizer(){
//        return openApi -> openApi.getPaths().values().forEach(
//                pathItem -> {
//                    pathItem.readOperations().forEach(
//                            operation -> {
//                                Parameter authHeader = new Parameter()
//                                        .in("header")
//                                        .name("Authorization")
//                                        .description("Access Token")
//                                        .required(false);
//                                operation.addParametersItem(authHeader);
//                            }
//                    );
//                }
//        );
//    }

}
