package com.keykiosk.Services;

import com.keykiosk.Models.Repository.VerificationCodeRepository;
import com.keykiosk.Util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    public void sendVerificationEmailAsync(String email, String verificationCode) {
        Runnable task = () -> {
            try {
                EmailUtil.sendVerificationEmail(email, verificationCode);

            } catch (Exception e) {
                System.err.println("Failed to send verification email: " + e.getMessage());
            }
        };

        Thread thread = new Thread(task);
        thread.start();
    }
}
