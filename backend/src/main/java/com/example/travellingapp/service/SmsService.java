package com.example.travellingapp.service;

import com.example.travellingapp.response_template.ResponseBody;

public interface SmsService {

    ResponseBody<String> sendSms(String userPhoneNumber, String sms);
}
