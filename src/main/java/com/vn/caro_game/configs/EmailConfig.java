package com.vn.caro_game.configs;

import com.vn.caro_game.constants.EmailConstants;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailConfig {
    
    @Value("${spring.mail.host}")
    String mailHost;
    
    @Value("${spring.mail.port}")
    int mailPort;
    
    @Value("${spring.mail.username}")
    String mailUsername;
    
    @Value("${spring.mail.password}")
    String mailPassword;
    
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put(EmailConstants.SMTP_AUTH_PROPERTY, EmailConstants.SMTP_AUTH_VALUE);
        props.put(EmailConstants.SMTP_STARTTLS_ENABLE_PROPERTY, EmailConstants.SMTP_STARTTLS_ENABLE_VALUE);
        props.put(EmailConstants.SMTP_STARTTLS_REQUIRED_PROPERTY, EmailConstants.SMTP_STARTTLS_REQUIRED_VALUE);
        props.put("mail.debug", "false");
        
        return mailSender;
    }
}
