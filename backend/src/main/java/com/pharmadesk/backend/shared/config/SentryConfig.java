package com.pharmadesk.backend.shared.config;

import io.sentry.Breadcrumb;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SentryConfig {

    @Bean
    public SentryOptions.BeforeSendCallback beforeSendCallback() {
        return (event, hint) -> {
            // Scrub PII from breadcrumbs
            if (event.getBreadcrumbs() != null) {
                for (Breadcrumb breadcrumb : event.getBreadcrumbs()) {
                    scrubPII(breadcrumb);
                }
            }
            return event;
        };
    }

    private void scrubPII(Breadcrumb breadcrumb) {
        if (breadcrumb.getMessage() != null) {
            String scrubbedMessage = breadcrumb.getMessage()
                    .replaceAll("\\b\\d{10}\\b", "[REDACTED_PHONE]");
            breadcrumb.setMessage(scrubbedMessage);
        }
        
        if (breadcrumb.getData() != null) {
            for (Map.Entry<String, Object> entry : breadcrumb.getData().entrySet()) {
                String key = entry.getKey().toLowerCase();
                if (key.contains("patient") || key.contains("name") || key.contains("phone") || key.contains("mobile")) {
                    breadcrumb.getData().put(entry.getKey(), "[REDACTED]");
                }
            }
        }
    }
}
