package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.SendEmailRequest;
import com.socialsecretariat.espacepartage.dto.SendEmailResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from}")
    private String fromEmail;
    
    public SendEmailResponse sendDocumentEmail(SendEmailRequest request, List<File> attachments) {
        try {
            log.info("Sending email to {} recipients with {} attachments", 
                    request.getRecipients().size(), attachments.size());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Set basic email properties
            helper.setFrom(fromEmail);
            helper.setTo(request.getRecipients().toArray(new String[0]));
            
            if (request.getCcRecipients() != null && !request.getCcRecipients().isEmpty()) {
                helper.setCc(request.getCcRecipients().toArray(new String[0]));
            }
            
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), false); // false = plain text
            
            // Add attachments
            List<String> attachmentNames = new ArrayList<>();
            for (File attachment : attachments) {
                if (attachment.exists()) {
                    FileSystemResource file = new FileSystemResource(attachment);
                    helper.addAttachment(attachment.getName(), file);
                    attachmentNames.add(attachment.getName());
                    log.debug("Added attachment: {}", attachment.getName());
                } else {
                    log.warn("Attachment file not found: {}", attachment.getAbsolutePath());
                }
            }
            
            // Send the email
            mailSender.send(message);
            
            log.info("Email sent successfully to: {}", String.join(", ", request.getRecipients()));
            
            // Create success response
            SendEmailResponse response = new SendEmailResponse();
            response.setSuccess(true);
            response.setMessage("Email envoyé avec succès à " + request.getRecipients().size() + " destinataire(s)");
            response.setRecipients(request.getRecipients());
            response.setCcRecipients(request.getCcRecipients());
            response.setSubject(request.getSubject());
            response.setSentAt(LocalDateTime.now());
            response.setAttachmentNames(attachmentNames);
            
            return response;
            
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
            
            SendEmailResponse response = new SendEmailResponse();
            response.setSuccess(false);
            response.setMessage("Erreur lors de l'envoi de l'email: " + e.getMessage());
            response.setRecipients(request.getRecipients());
            response.setCcRecipients(request.getCcRecipients());
            response.setSubject(request.getSubject());
            
            return response;
        }
    }
    
    public boolean isEmailConfigured() {
        try {
            // Try to create a test message to verify configuration
            MimeMessage testMessage = mailSender.createMimeMessage();
            return testMessage != null;
        } catch (Exception e) {
            log.warn("Email configuration test failed", e);
            return false;
        }
    }
}
