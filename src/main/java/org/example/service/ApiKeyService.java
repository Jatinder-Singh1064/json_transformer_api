package org.example.service;

import org.example.model.UserEntity;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
public class ApiKeyService {

    @Autowired
    private UserRepository userRepository;

    private static final SecureRandom secureRandom = new SecureRandom(); // Thread-safe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    // Generate a new key when a new user registers with their email
    public String generateNewKey(String email) {
        // 1. Generate a random 32-byte array (256 bits of entropy)
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        // 2. Encode to a URL-safe String
        String newKey = "sk_" + base64Encoder.encodeToString(randomBytes).substring(0, 32);

        // 3. Save to database for Week 2/3 tracking
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setApiKey(newKey);
        user.setRequestCount(0);
        userRepository.save(user);

        return newKey;
    }

    // Fetch all users from the database
    public List<UserEntity> getAllUsageReports() {
        return userRepository.findAll();
    }
}
