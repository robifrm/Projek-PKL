package com.vnet.vnet_backend.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private static class CaptchaDetails {
        String answer;
        long expiryTime;

        CaptchaDetails(String answer, long expiryTime) {
            this.answer = answer;
            this.expiryTime = expiryTime;
        }
    }

    private final Map<String, CaptchaDetails> captchaStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public Map<String, String> generateCaptcha() {
        // Bersihkan entri kedaluwarsa
        long now = System.currentTimeMillis();
        captchaStorage.entrySet().removeIf(entry -> entry.getValue().expiryTime < now);

        int num1 = random.nextInt(15) + 1; // 1 s/d 15
        int num2 = random.nextInt(10) + 1; // 1 s/d 10
        boolean isPlus = random.nextBoolean();

        String question;
        int result;
        if (isPlus) {
            question = num1 + " + " + num2 + " = ?";
            result = num1 + num2;
        } else {
            if (num1 < num2) {
                int temp = num1;
                num1 = num2;
                num2 = temp;
            }
            question = num1 + " - " + num2 + " = ?";
            result = num1 - num2;
        }

        String captchaId = UUID.randomUUID().toString();
        long expiry = now + (3 * 60 * 1000); // Valid 3 menit

        captchaStorage.put(captchaId, new CaptchaDetails(String.valueOf(result), expiry));

        Map<String, String> captchaResponse = new HashMap<>();
        captchaResponse.put("captchaId", captchaId);
        captchaResponse.put("question", question);
        return captchaResponse;
    }

    public boolean validateCaptcha(String captchaId, String answer) {
        if (captchaId == null || answer == null) {
            return false;
        }
        CaptchaDetails details = captchaStorage.remove(captchaId); // Satu kali pakai
        if (details == null) {
            return false;
        }
        if (System.currentTimeMillis() > details.expiryTime) {
            return false;
        }
        return details.answer.trim().equals(answer.trim());
    }
}
