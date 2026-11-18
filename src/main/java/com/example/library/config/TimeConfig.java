package com.example.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/** Inject Clock thay vì gọi LocalDate.now() trực tiếp để unit test cố định được ngày. */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Ho_Chi_Minh"));
    }
}
