package com.keykiosk.Util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EmailUtil {
    private static final Map<String, String> emailVerificationMap = new HashMap<>();

    public static void sendVerificationEmail(String toEmail, String verificationCode) {
        // Thêm địa chỉ email và mã xác nhận vào bản đồ
        emailVerificationMap.put(toEmail, verificationCode);

        // Thông tin tài khoản email gửi
        final String username = "z1001st.com@gmail.com";
        final String password = "uibjojaewbkfxtxv";
        // Cấu hình thông tin máy chủ email
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Tạo một phiên gửi email
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Tạo tin nhắn email
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Email Verification");
            message.setText("Your verification code is: " + verificationCode);

            // Gửi email
            Transport.send(message);

            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
