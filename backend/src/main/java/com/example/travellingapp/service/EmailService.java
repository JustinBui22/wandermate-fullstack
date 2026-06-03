package com.example.travellingapp.service;


public interface EmailService {

    void sendEmail(String senderEmail, String receiverEmail, String emailSubject, String emailContent);
}
