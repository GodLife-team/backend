package com.god.life.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class GoogleStorageConfig {

    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String fileName;

    @Value("${spring.cloud.gcp.storage.project-id}")
    private String projectId;
    @Bean
    public Storage storage() throws IOException {
        ClassPathResource resources = new ClassPathResource(fileName);
        GoogleCredentials credentials = GoogleCredentials.fromStream(resources.getInputStream());

        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }

}

