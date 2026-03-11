package com.hsf.hsf302_ecom.service;

public interface EmailService {

    /**
     * Send a 6-digit verification code to the given address.
     * Subject will be "Voltex – Activate your account".
     */
    void sendActivationCode(String to, String code);

    /**
     * Send a 6-digit password-reset code to the given address.
     * Subject will be "Voltex – Reset your password".
     */
    void sendPasswordResetCode(String to, String code);
}