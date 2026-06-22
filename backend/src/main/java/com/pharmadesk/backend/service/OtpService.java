package com.pharmadesk.backend.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class OtpService {

    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public void sendOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        otpStore.put(email, otp);
        System.out.println("DEBUG: OTP sent to " + email + " is " + otp);
        // In production, integrate with email sending service or logger
    }

    public boolean verifyOtp(String email, String otp) {
        if (email == null || otp == null) return false;
        String storedOtp = otpStore.get(email);
        if (otp.equals(storedOtp)) {
            otpStore.remove(email); // consume OTP
            return true;
        }
        return false;
    }
}
