package com.pablochomcobar.cloudnotes.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Handles Firebase Auth token verification from the frontend
 * and manages user login/logout session in Spring Boot.
 */
@Controller
public class AuthController {

    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USER_NAME = "userName";
    public static final String SESSION_USER_EMAIL = "userEmail";

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "idToken is required"));
        }

        try {
            // Verify ID Token with Firebase Admin SDK
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String uid = decodedToken.getUid();
            String name = decodedToken.getName() != null ? decodedToken.getName() : decodedToken.getEmail();
            String email = decodedToken.getEmail();

            // Store in session
            session.setAttribute(SESSION_USER_ID, uid);
            session.setAttribute(SESSION_USER_NAME, name);
            session.setAttribute(SESSION_USER_EMAIL, email);

            return ResponseEntity.ok(Map.of("status", "success", "uid", uid, "name", name != null ? name : ""));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Firebase token: " + e.getMessage()));
        }
    }

    @PostMapping("/api/auth/logout")
    @ResponseBody
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("status", "logged_out"));
    }
}
