package com.hsf.hsf302_ecom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendActivationCode(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Voltex – Activate your account");
        msg.setText(
                "Hello,\n\n" +
                        "Your Voltex account activation code is:\n\n" +
                        "  " + code + "\n\n" +
                        "This code expires in 10 minutes.\n\n" +
                        "If you did not create a Voltex account, please ignore this email.\n\n" +
                        "— Voltex Electronics"
        );
        mailSender.send(msg);
    }

    @Override
    public void sendPasswordResetCode(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Voltex – Reset your password");
        msg.setText(
                "Hello,\n\n" +
                        "Your Voltex password reset code is:\n\n" +
                        "  " + code + "\n\n" +
                        "This code expires in 10 minutes.\n\n" +
                        "If you did not request a password reset, please ignore this email.\n\n" +
                        "— Voltex Electronics"
        );
        mailSender.send(msg);
    }
}