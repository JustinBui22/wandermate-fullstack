package com.example.travellingapp.config;

import com.example.travellingapp.security.AccountEnumerationRateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AccountEnumerationRateLimitInterceptor
            accountEnumerationRateLimitInterceptor;

    public WebMvcConfig(
            AccountEnumerationRateLimitInterceptor accountEnumerationRateLimitInterceptor
    ) {
        this.accountEnumerationRateLimitInterceptor =
                accountEnumerationRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accountEnumerationRateLimitInterceptor)
                .addPathPatterns(
                        "/api/v1/users/register/verify",
                        "/api/v1/otp/send"
                );
    }
}
