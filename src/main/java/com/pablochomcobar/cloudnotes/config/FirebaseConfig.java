package com.pablochomcobar.cloudnotes.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Sets up the Firebase Admin SDK and exposes a Firestore object as a Spring Bean.
 *
 * The service-account key (the JSON file you download from Firebase) is looked up
 * in this order, so the same code works on your PC and on Render:
 *
 *  1. FIREBASE_CREDENTIALS environment variable - the whole JSON text (or Base64 of it).
 *     Best for Render.
 *  2. firebase.credentials.path property / FIREBASE_CREDENTIALS_PATH env var - a file path.
 *  3. serviceAccountKey.json in the project root folder (easiest for local testing).
 *  4. serviceAccountKey.json inside src/main/resources.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);
    private static final String DEFAULT_FILE = "serviceAccountKey.json";

    // Values come from application.properties (which can read environment variables)
    @Value("${firebase.credentials.json:}")
    private String credentialsJson;

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    /**
     * Creates (only once) the FirebaseApp and returns a Firestore client.
     * Because this is a @Bean, Spring can inject Firestore anywhere we need it.
     */
    @Bean
    public Firestore firestore() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            try (InputStream serviceAccount = openCredentials()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully.");
            }
        }
        return FirestoreClient.getFirestore();
    }

    /** Finds the service-account JSON using the order described above. */
    private InputStream openCredentials() throws IOException {
        // 1. JSON (or Base64 JSON) stored directly in an environment variable
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            String json = credentialsJson.trim();
            if (!json.startsWith("{")) {
                // Not raw JSON, so treat it as Base64 encoded
                json = new String(Base64.getDecoder().decode(json), StandardCharsets.UTF_8);
            }
            log.info("Loading Firebase credentials from FIREBASE_CREDENTIALS environment variable.");
            return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        }

        // 2. Explicit file path
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            log.info("Loading Firebase credentials from file: {}", credentialsPath);
            return new FileInputStream(credentialsPath);
        }

        // 3. File in the project root / working directory
        Path rootFile = Path.of(DEFAULT_FILE);
        if (Files.exists(rootFile)) {
            log.info("Loading Firebase credentials from ./{}", DEFAULT_FILE);
            return Files.newInputStream(rootFile);
        }

        // 4. File inside src/main/resources
        ClassPathResource resource = new ClassPathResource(DEFAULT_FILE);
        if (resource.exists()) {
            log.info("Loading Firebase credentials from classpath:{}", DEFAULT_FILE);
            return resource.getInputStream();
        }

        // Nothing found: stop with a clear, beginner-friendly message
        throw new IllegalStateException(
                "Firebase credentials not found! Put 'serviceAccountKey.json' in the project root folder, "
                + "or set the FIREBASE_CREDENTIALS environment variable. See README.md.");
    }
}
