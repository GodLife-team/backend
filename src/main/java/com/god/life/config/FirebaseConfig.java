package com.god.life.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {
    private final String fcmServiceFile;

    public FirebaseConfig(@Value("${fcm.file}") String fcmServiceFile) {
        this.fcmServiceFile = fcmServiceFile;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() throws Exception {
        try {
            ClassPathResource credentials = new ClassPathResource(fcmServiceFile);
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials.getInputStream())).build();
            FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
            return FirebaseMessaging.getInstance(firebaseApp);
        } catch (Exception e){
            e.printStackTrace();
        }
        throw new Exception("초기화 실패!");
    }

}
