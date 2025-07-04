package com.socialsecretariat.espacepartage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {
    private UUID documentGenerationId;
    private List<String> recipients;
    private List<String> ccRecipients;
    private String subject;
    private String body;
    private boolean includePdf = true;
    private boolean includeDocx = false;
}
