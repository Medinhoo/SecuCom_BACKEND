package com.socialsecretariat.espacepartage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailResponse {
    private boolean success;
    private String message;
    private List<String> recipients;
    private List<String> ccRecipients;
    private String subject;
    private String messageId;
    private LocalDateTime sentAt;
    private List<String> attachmentNames;
}
