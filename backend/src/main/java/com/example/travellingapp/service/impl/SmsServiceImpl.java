package com.example.travellingapp.service.impl;

import com.example.travellingapp.service.SmsService;
import com.example.travellingapp.response_template.ResponseBody;
import org.springframework.stereotype.Service;

import static com.example.travellingapp.enums.CommonEnum.SMS;
import static com.example.travellingapp.enums.ErrorCodeEnum.SMS_SENT_SUCCESS;

@Service
public class SmsServiceImpl implements SmsService {
    @Override
    public ResponseBody<String> sendSms(String userPhoneNumber, String sms) {

        return new ResponseBody<>(SMS_SENT_SUCCESS.getCode(), null, SMS.name(), null);
    }
}
